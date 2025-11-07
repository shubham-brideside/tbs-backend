#!/bin/bash

# Script to create sample blog data for testing
# Make sure your backend is running on localhost:8080

BASE_URL="http://localhost:8080/api/blog"

echo "Creating Wedding category..."
CATEGORY_RESPONSE=$(curl -s -X POST "$BASE_URL/categories" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wedding",
    "slug": "wedding",
    "description": "Myths. Trends. Rituals. Planning. Photography. Decoration. Dishes. Budgeting. Dance. Songs. Under this section, you will find articles related to weddings and only weddings. We want to make your special day more special, and these articles can surely help you do that! Lets learn about weddings in the best way possible!",
    "featuredImageUrl": "https://images.unsplash.com/photo-1519741497674-611481863552?w=800",
    "isActive": true
  }')

echo "Category created: $CATEGORY_RESPONSE"

# Extract category ID from response (assuming it's in the JSON)
CATEGORY_ID=1

echo ""
echo "Creating blog posts..."

# Post 1: Mehndi Designs
curl -s -X POST "$BASE_URL/posts" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"213+ Simple Mehndi Designs: Unique, Stylish, and Latest Designs\",
    \"slug\": \"213-simple-mehndi-designs-unique-stylish-latest\",
    \"excerpt\": \"Discover over 213 beautiful and simple mehndi designs perfect for weddings, festivals, and special occasions. From traditional to modern styles, find the perfect design for your hands.\",
    \"content\": \"<h2>Introduction to Mehndi Designs</h2><p>Mehndi, also known as henna, is an integral part of Indian weddings and festivals. Its not just a form of body art but a celebration of tradition and beauty.</p>\",
    \"featuredImageUrl\": \"https://images.unsplash.com/photo-1584464491033-06628f3a6b7b?w=800\",
    \"categoryId\": $CATEGORY_ID,
    \"metaDescription\": \"213+ simple mehndi designs for weddings and festivals\",
    \"metaKeywords\": \"mehndi designs, henna designs, simple mehndi, wedding mehndi\",
    \"isPublished\": true
  }"
echo " - Post 1 created"

# Post 2: Haldi Decoration
curl -s -X POST "$BASE_URL/posts" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"100+ Latest Haldi Decoration Ideas at Home and Outdoor Events\",
    \"slug\": \"100-latest-haldi-decoration-ideas-home-outdoor\",
    \"excerpt\": \"Transform your Haldi ceremony into an Instagram-worthy event with these stunning decoration ideas. From simple DIY setups to grand outdoor arrangements.\",
    \"content\": \"<h2>Haldi Decoration Ideas by Budget</h2><p>Your budget should not limit your ability to create a stunning Haldi setup.</p>\",
    \"featuredImageUrl\": \"https://images.unsplash.com/photo-1519741497674-611481863552?w=800\",
    \"categoryId\": $CATEGORY_ID,
    \"metaDescription\": \"100+ latest Haldi decoration ideas for home and outdoor events\",
    \"metaKeywords\": \"haldi decoration, haldi ceremony, wedding decoration\",
    \"isPublished\": true
  }"
echo " - Post 2 created"

# Post 3: Goa Wedding Venues
curl -s -X POST "$BASE_URL/posts" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"The 10 Best Destination Wedding Venues in Goa | Beach Wedding Planning & Cost\",
    \"slug\": \"10-best-destination-wedding-venues-goa-beach-wedding\",
    \"excerpt\": \"Planning a dream beach wedding in Goa? Discover the top 10 destination wedding venues, complete planning guide, and cost breakdown.\",
    \"content\": \"<h2>Why Choose Goa for Your Destination Wedding?</h2><p>Goa offers the perfect blend of beautiful beaches and tropical weather.</p>\",
    \"featuredImageUrl\": \"https://images.unsplash.com/photo-1519167758481-83f29da1c4fe?w=800\",
    \"categoryId\": $CATEGORY_ID,
    \"metaDescription\": \"Top 10 destination wedding venues in Goa\",
    \"metaKeywords\": \"goa wedding, destination wedding, beach wedding\",
    \"isPublished\": true
  }"
echo " - Post 3 created"

# Post 4: Kids Mehndi
curl -s -X POST "$BASE_URL/posts" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"100+ Easy and Simple Kids Mehndi Designs Photos\",
    \"slug\": \"100-easy-simple-kids-mehndi-designs-photos\",
    \"excerpt\": \"Looking for cute and simple mehndi designs for kids? Browse through 100+ easy patterns perfect for little hands.\",
    \"content\": \"<h2>Why Mehndi for Kids?</h2><p>Mehndi is a fun way to celebrate festivals with kids.</p>\",
    \"featuredImageUrl\": \"https://images.unsplash.com/photo-1606092195730-5d7b9af1efc5?w=800\",
    \"categoryId\": $CATEGORY_ID,
    \"metaDescription\": \"100+ easy and simple mehndi designs for kids\",
    \"metaKeywords\": \"kids mehndi, children mehndi, simple mehndi\",
    \"isPublished\": true
  }"
echo " - Post 4 created"

# Post 5: Wedding Planning Checklist
curl -s -X POST "$BASE_URL/posts" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Complete Wedding Planning Checklist: From Engagement to Reception\",
    \"slug\": \"complete-wedding-planning-checklist-engagement-reception\",
    \"excerpt\": \"Never miss a detail with this comprehensive wedding planning checklist. Covering everything from engagement to reception.\",
    \"content\": \"<h2>12 Months Before Wedding</h2><p>Start your wedding planning journey with these essential tasks.</p>\",
    \"featuredImageUrl\": \"https://images.unsplash.com/photo-1519225421980-715cb0215aed?w=800\",
    \"categoryId\": $CATEGORY_ID,
    \"metaDescription\": \"Complete wedding planning checklist from engagement to reception\",
    \"metaKeywords\": \"wedding planning, wedding checklist, wedding guide\",
    \"isPublished\": true
  }"
echo " - Post 5 created"

# Post 6: Photography Tips
curl -s -X POST "$BASE_URL/posts" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Top 15 Wedding Photography Tips for Perfect Pictures\",
    \"slug\": \"top-15-wedding-photography-tips-perfect-pictures\",
    \"excerpt\": \"Capture every magical moment of your wedding day with these essential photography tips.\",
    \"content\": \"<h2>Choosing the Right Wedding Photographer</h2><p>Your photographer is one of the most important vendors for your wedding.</p>\",
    \"featuredImageUrl\": \"https://images.unsplash.com/photo-1519741497674-611481863552?w=800\",
    \"categoryId\": $CATEGORY_ID,
    \"metaDescription\": \"Top 15 wedding photography tips for perfect pictures\",
    \"metaKeywords\": \"wedding photography, wedding photos, photography tips\",
    \"isPublished\": true
  }"
echo " - Post 6 created"

echo ""
echo "Sample blog data created successfully!"
echo "You can now view the posts at: http://localhost:8080/api/blog/posts"

