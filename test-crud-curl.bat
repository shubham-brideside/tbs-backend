@echo off
REM Comprehensive CRUD Operations Test Script using curl (Windows Batch)
REM This script tests all Create, Read, Update, Delete operations

set BASE_URL=http://localhost:8080/api/deals
set HEALTH_URL=http://localhost:8080/actuator/health

echo === COMPREHENSIVE CRUD OPERATIONS TEST (curl) ===
echo Base URL: %BASE_URL%
echo.

REM 1. HEALTH CHECK
echo 1. HEALTH CHECK
curl -s -X GET "%HEALTH_URL%"
echo.
echo.

REM 2. CREATE OPERATIONS (POST)
echo 2. CREATE OPERATIONS (POST)

echo Creating single deal...
curl -s -X POST -H "Content-Type: application/json" -d "{\"name\":\"John Doe\",\"contact_number\":\"9876543210\",\"categories\":[{\"name\":\"Photography\",\"event_date\":\"2025-12-25\",\"venue\":\"Grand Hotel, Mumbai\",\"budget\":50000,\"expected_gathering\":150}]}" "%BASE_URL%"
echo.
echo.

echo Creating multiple deals...
curl -s -X POST -H "Content-Type: application/json" -d "{\"name\":\"Jane Smith\",\"contact_number\":\"8765432109\",\"categories\":[{\"name\":\"Catering\",\"event_date\":\"2025-11-15\",\"venue\":\"Taj Hotel, Delhi\",\"budget\":75000,\"expected_gathering\":100},{\"name\":\"Decoration\",\"event_date\":\"2025-11-15\",\"venue\":\"Taj Hotel, Delhi\",\"budget\":30000,\"expected_gathering\":100}]}" "%BASE_URL%"
echo.
echo.

REM 3. READ OPERATIONS (GET)
echo 3. READ OPERATIONS (GET)

echo Getting all deals...
curl -s -X GET "%BASE_URL%"
echo.
echo.

echo Getting deal by ID (1)...
curl -s -X GET "%BASE_URL%/1"
echo.
echo.

echo Getting deals by user (John Doe)...
curl -s -X GET "%BASE_URL%/user/John%%20Doe"
echo.
echo.

echo Getting deals by contact (9876543210)...
curl -s -X GET "%BASE_URL%/contact/9876543210"
echo.
echo.

echo Getting deals by category (Photography)...
curl -s -X GET "%BASE_URL%/category/Photography"
echo.
echo.

REM 4. UPDATE OPERATIONS (PUT)
echo 4. UPDATE OPERATIONS (PUT)

echo Updating deal by ID (1)...
curl -s -X PUT -H "Content-Type: application/json" -d "{\"name\":\"John Doe Updated\",\"contact_number\":\"9876543210\",\"categories\":[{\"name\":\"Photography\",\"event_date\":\"2025-12-30\",\"venue\":\"Updated Grand Hotel, Mumbai\",\"budget\":60000,\"expected_gathering\":200}]}" "%BASE_URL%/1"
echo.
echo.

REM 5. DELETE OPERATIONS (DELETE)
echo 5. DELETE OPERATIONS (DELETE)

echo Deleting deal by ID (2)...
curl -s -X DELETE "%BASE_URL%/2"
echo.
echo.

echo Deleting all deals for user (Jane Smith)...
curl -s -X DELETE "%BASE_URL%/user/Jane%%20Smith"
echo.
echo.

REM 6. FINAL VERIFICATION
echo 6. FINAL VERIFICATION

echo Getting all remaining deals...
curl -s -X GET "%BASE_URL%"
echo.
echo.

echo === CRUD OPERATIONS TEST COMPLETE ===
echo Check the results above to verify all operations are working correctly.
pause
