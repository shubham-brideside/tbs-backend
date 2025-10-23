package com.brideside.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pipedrive")
public class PipedriveProperties {
    
    private Api api = new Api();
    private Deal deal = new Deal();
    private Person person = new Person();

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public Deal getDeal() {
        return deal;
    }

    public void setDeal(Deal deal) {
        this.deal = deal;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
    
    public static class Api {
        private String token;
        private String baseUrl;
        private Integer orgId;
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public Integer getOrgId() {
            return orgId;
        }
        
        public void setOrgId(Integer orgId) {
            this.orgId = orgId;
        }
    }
    
    public static class Deal {
        private Integer pipelineId;
        private CustomFields customFields = new CustomFields();
        
        public Integer getPipelineId() {
            return pipelineId;
        }
        
        public void setPipelineId(Integer pipelineId) {
            this.pipelineId = pipelineId;
        }
        
        public CustomFields getCustomFields() {
            return customFields;
        }
        
        public void setCustomFields(CustomFields customFields) {
            this.customFields = customFields;
        }
        
        public static class CustomFields {
            private String eventType;
            private String eventDate;
            private String venue;
            private String dealSource;
            
            public String getEventType() {
                return eventType;
            }
            
            public void setEventType(String eventType) {
                this.eventType = eventType;
            }
            
            public String getEventDate() {
                return eventDate;
            }
            
            public void setEventDate(String eventDate) {
                this.eventDate = eventDate;
            }
            
            public String getVenue() {
                return venue;
            }
            
            public void setVenue(String venue) {
                this.venue = venue;
            }
            
            public String getDealSource() {
                return dealSource;
            }
            
            public void setDealSource(String dealSource) {
                this.dealSource = dealSource;
            }
        }
    }
    
    public static class Person {
        private CustomFields customFields = new CustomFields();
        
        public CustomFields getCustomFields() {
            return customFields;
        }
        
        public void setCustomFields(CustomFields customFields) {
            this.customFields = customFields;
        }
        
        public static class CustomFields {
            private String personSource;
            
            public String getPersonSource() {
                return personSource;
            }
            
            public void setPersonSource(String personSource) {
                this.personSource = personSource;
            }
        }
    }
}
