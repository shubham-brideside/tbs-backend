# Pipedrive Integration Guide

## Overview

This document describes the Pipedrive integration implemented in the Brideside Backend API. The integration automatically creates contacts and deals in Pipedrive when users interact with the deal management system.

## Features

### 1. Contact Management
- **Automatic Contact Creation**: When a deal is initialized with a contact number, a person is automatically created in Pipedrive
- **Contact Deduplication**: Prevents duplicate contacts by checking existing contacts before creating new ones
- **Local Contact Storage**: All contacts are stored locally in the `contacts` table with Pipedrive contact IDs

### 2. Deal Management
- **Automatic Deal Creation**: Each deal category creates a separate deal in Pipedrive
- **Custom Field Mapping**: Maps deal data to Pipedrive custom fields:
  - Event Type → `PIPEDRIVE_DEAL_EVENT_TYPE_FIELD`
  - Event Date → `PIPEDRIVE_DEAL_EVENT_DATE_FIELD`
  - Venue → `PIPEDRIVE_DEAL_VENUE_FIELD`
- **Local Deal Storage**: All deals are stored locally with Pipedrive deal IDs

### 3. Error Handling
- **Graceful Degradation**: If Pipedrive is unavailable, the system continues to work locally
- **Retry Logic**: Failed Pipedrive operations don't break the core functionality
- **Logging**: Comprehensive logging for debugging Pipedrive integration issues

## Configuration

### Timezone Configuration

The application is configured to use IST (Indian Standard Time) for all timestamps:
- Database timezone: `Asia/Kolkata`
- JVM timezone: `Asia/Kolkata`
- Jackson timezone: `Asia/Kolkata`

### Environment Variables

Set these environment variables in your deployment:

```bash
# Pipedrive API Configuration
PIPEDRIVE_API_TOKEN=your_pipedrive_api_token
PIPEDRIVE_BASE_URL=https://acceltancy4.pipedrive.com
PIPEDRIVE_ORG_ID=41
PIPEDRIVE_PIPELINE_ID=13

# Pipedrive Custom Field IDs
PIPEDRIVE_DEAL_EVENT_TYPE_FIELD=33be05e5ce4039a0019fac9341f93e55f651613b
PIPEDRIVE_DEAL_EVENT_DATE_FIELD=df4c9016566220a8b31b23daf8658ebf868fa703
PIPEDRIVE_DEAL_VENUE_FIELD=202deab858d537436beba56583230b4a5bd61d47
PIPEDRIVE_DEAL_SOURCE_FIELD=ae66e7601c2787449fe3e796ac803fc62645a22b
```

### Application Properties

The configuration is also available in `application.yml`:

```yaml
pipedrive:
  api:
    token: ${PIPEDRIVE_API_TOKEN:your_pipedrive_api_token}
    base-url: ${PIPEDRIVE_BASE_URL:https://acceltancy4.pipedrive.com}
    org-id: ${PIPEDRIVE_ORG_ID:41}
  deal:
    pipeline-id: ${PIPEDRIVE_PIPELINE_ID:13}
    custom-fields:
      event-type: ${PIPEDRIVE_DEAL_EVENT_TYPE_FIELD:33be05e5ce4039a0019fac9341f93e55f651613b}
      event-date: ${PIPEDRIVE_DEAL_EVENT_DATE_FIELD:df4c9016566220a8b31b23daf8658ebf868fa703}
      venue: ${PIPEDRIVE_DEAL_VENUE_FIELD:202deab858d537436beba56583230b4a5bd61d47}
      deal-source: ${PIPEDRIVE_DEAL_SOURCE_FIELD:ae66e7601c2787449fe3e796ac803fc62645a22b}
```

## Database Schema

### Contacts Table

```sql
CREATE TABLE `contacts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `contact_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pipedrive_contact_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ix_contacts_contact_name` (`contact_name`),
  KEY `ix_contacts_id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Updated Deals Table

```sql
CREATE TABLE `deals` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_name` varchar(100) NOT NULL,
  `contact_number` varchar(20) NOT NULL,
  `category` varchar(50) NOT NULL,
  `event_date` date DEFAULT NULL,
  `venue` varchar(255) DEFAULT NULL,
  `budget` decimal(10,2) DEFAULT NULL,
  `expected_gathering` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `pipedrive_deal_id` varchar(100) DEFAULT NULL,
  `contact_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_name` (`user_name`),
  INDEX `idx_contact_number` (`contact_number`),
  INDEX `idx_category` (`category`),
  INDEX `idx_event_date` (`event_date`),
  INDEX `idx_contact_id` (`contact_id`),
  FOREIGN KEY (`contact_id`) REFERENCES `contacts`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

## API Flow

### 1. Deal Initialization (`POST /api/deals/init`)

When a deal is initialized:

1. **Check for existing deals** with the same contact number
2. **Create contact in Pipedrive** (if new contact)
3. **Create deal in Pipedrive** (placeholder deal)
4. **Save contact locally** with Pipedrive contact ID
5. **Save deal locally** with Pipedrive deal ID and contact ID

### 2. Deal Details Update (`PUT /api/deals/{id}/details`)

When deal details are updated:

1. **Retrieve existing deal** and associated contact
2. **Update contact name** in Pipedrive (if was "TBS")
3. **For each category**:
   - Create new deal in Pipedrive
   - Update Pipedrive deal with custom fields
   - Save deal locally with Pipedrive deal ID
4. **Delete original placeholder deal**

## Pipedrive API Endpoints Used

### Create Person
```
POST https://acceltancy4.pipedrive.com/api/v1/persons?api_token=your_token
```

**Request Body:**
```json
{
  "name": "John Doe",
  "phone": "+1234567890",
  "org_id": 41
}
```

### Create Deal
```
POST https://acceltancy4.pipedrive.com/api/v1/deals?api_token=your_token
```

**Request Body:**
```json
{
  "title": "Wedding Photography",
  "value": 5000,
  "currency": "USD",
  "person_id": 12,
  "pipeline_id": 13,
  "org_id": 41,
  "status": "open"
}
```

### Update Deal with Custom Fields
```
PUT https://acceltancy4.pipedrive.com/api/v1/deals/{deal_id}?api_token=your_token
```

**Request Body:**
```json
{
  "accd9fb1f4d3f8908a76936061144431b98352e6": "Wedding Photography",
  "7b61d5c385508aa4cef1a30d7c3b350209670f39": "2024-06-15",
  "477fb2ebb13a363c7bf614255f42c8133c9f2447": "Grand Hotel",
  "ae66e7601c2787449fe3e796ac803fc62645a22b": "TBS Landing Page"
}
```

## Testing

### Test Deal Initialization

```bash
curl -X POST 'http://localhost:8080/api/deals/init' \
  -H 'Content-Type: application/json' \
  -d '{
    "contact_number": "+1234567890"
  }'
```

### Test Deal Details Update

```bash
curl -X PUT 'http://localhost:8080/api/deals/{id}/details' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "John Doe",
    "categories": [
      {
        "name": "Wedding Photography",
        "event_date": "2024-06-15",
        "venue": "Grand Hotel",
        "budget": 5000,
        "expected_gathering": 150
      }
    ]
  }'
```

## Monitoring and Troubleshooting

### Logs to Monitor

1. **Pipedrive API calls**: Look for "Successfully created person/deal in Pipedrive"
2. **Error handling**: Look for "Error creating/updating in Pipedrive"
3. **Fallback behavior**: Look for "Pipedrive integration fails, continuing locally"

### Common Issues

1. **Invalid API Token**: Check `PIPEDRIVE_API_TOKEN` environment variable
2. **Wrong Base URL**: Verify `PIPEDRIVE_BASE_URL` format
3. **Custom Field IDs**: Ensure custom field IDs match your Pipedrive setup
4. **Network Issues**: Check connectivity to Pipedrive API

### Health Checks

The system includes health checks for:
- Database connectivity
- Pipedrive API availability (optional)
- Application status

## Security Considerations

1. **API Token Security**: Store Pipedrive API token securely
2. **Data Privacy**: Ensure compliance with data protection regulations
3. **Rate Limiting**: Be aware of Pipedrive API rate limits
4. **Error Information**: Don't expose sensitive error details in responses

## Future Enhancements

1. **Contact Updates**: Implement contact update functionality in Pipedrive
2. **Deal Updates**: Add deal update capabilities
3. **Webhook Integration**: Handle Pipedrive webhooks for real-time sync
4. **Bulk Operations**: Optimize for bulk contact/deal creation
5. **Sync Status**: Add sync status tracking for better monitoring
