# JMeter Load Testing Guide for Deals API

## 🚀 **Quick Start**

### **1. Prerequisites**
- Download and install JMeter from: https://jmeter.apache.org/download_jmeter.cgi
- Ensure your Spring Boot application is running on `http://localhost:8080`
- Java 8+ installed

### **2. Test Plans Available**

#### **Simple Test Plan** (`simple-jmeter-test.jmx`)
- **20 concurrent users**
- **30-second ramp-up**
- **5 iterations per user**
- **Tests:** Create Deal + Get All Deals
- **Duration:** ~2.5 minutes

#### **Comprehensive Test Plan** (`jmeter-deals-api.jmx`)
- **50 concurrent users**
- **60-second ramp-up**
- **10 iterations per user**
- **Tests:** All CRUD operations
- **Duration:** ~10 minutes

## 📊 **Running Load Tests**

### **Method 1: GUI Mode (Recommended for beginners)**

1. **Open JMeter GUI:**
   ```bash
   # Windows
   jmeter.bat
   
   # Linux/Mac
   ./jmeter.sh
   ```

2. **Load Test Plan:**
   - File → Open → Select `simple-jmeter-test.jmx` or `jmeter-deals-api.jmx`

3. **Configure Test (Optional):**
   - Right-click on "Thread Group"
   - Adjust number of users, ramp-up time, or iterations

4. **Run Test:**
   - Click the green "Start" button (▶️)
   - Monitor results in real-time

5. **View Results:**
   - Check "Summary Report" for statistics
   - View "View Results Tree" for detailed responses

### **Method 2: Command Line Mode (Recommended for production)**

```bash
# Simple test
jmeter -n -t simple-jmeter-test.jmx -l results-simple.jtl -e -o reports-simple/

# Comprehensive test
jmeter -n -t jmeter-deals-api.jmx -l results-comprehensive.jtl -e -o reports-comprehensive/
```

**Parameters:**
- `-n`: Non-GUI mode
- `-t`: Test plan file
- `-l`: Results log file
- `-e`: Generate HTML report
- `-o`: Output directory for HTML report

## 📈 **Understanding Results**

### **Key Metrics to Monitor:**

1. **Response Time:**
   - **Average:** Should be < 500ms
   - **95th Percentile:** Should be < 1000ms
   - **Max:** Should be < 2000ms

2. **Throughput:**
   - **Requests per second:** Higher is better
   - **Target:** 50+ requests/second

3. **Error Rate:**
   - **Should be < 1%**
   - **0% is ideal**

4. **Success Rate:**
   - **Should be > 99%**
   - **100% is ideal**

### **Sample Results Interpretation:**

```
Summary Report
==============
Samples: 1000
Average: 245ms
Median: 200ms
90% Line: 400ms
95% Line: 500ms
99% Line: 800ms
Min: 50ms
Max: 1200ms
Error%: 0.1%
Throughput: 45.2/sec
```

**✅ Good Performance:**
- Average response time < 500ms
- 95th percentile < 1000ms
- Error rate < 1%
- Throughput > 40 req/sec

## 🔧 **Customizing Load Tests**

### **Adjusting Load Parameters:**

1. **Number of Users:**
   - Right-click "Thread Group" → Properties
   - Change "Number of Threads (users)"

2. **Ramp-up Time:**
   - Change "Ramp-up period (seconds)"
   - **10 users, 10s ramp-up** = 1 user per second
   - **50 users, 60s ramp-up** = Gradual increase

3. **Test Duration:**
   - Check "Scheduler" checkbox
   - Set "Duration (seconds)"

### **Different Load Patterns:**

#### **Light Load Test:**
- Users: 10
- Ramp-up: 30s
- Duration: 5 minutes

#### **Medium Load Test:**
- Users: 50
- Ramp-up: 60s
- Duration: 10 minutes

#### **Heavy Load Test:**
- Users: 100
- Ramp-up: 120s
- Duration: 15 minutes

#### **Spike Test:**
- Users: 200
- Ramp-up: 10s
- Duration: 5 minutes

## 📊 **Monitoring During Tests**

### **Application Monitoring:**

1. **Check Application Logs:**
   ```bash
   tail -f logs/brideside-backend.log
   ```

2. **Monitor Database:**
   - Check MySQL performance
   - Monitor connection pool usage

3. **System Resources:**
   - CPU usage
   - Memory usage
   - Network I/O

### **JMeter Monitoring:**

1. **Real-time Results:**
   - View Results Tree (detailed)
   - Summary Report (statistics)
   - Response Time Graph

2. **Key Listeners:**
   - Response Time Graph
   - Active Threads Over Time
   - Transactions per Second

## 🚨 **Troubleshooting**

### **Common Issues:**

1. **High Error Rate:**
   - Check application logs
   - Verify database connections
   - Check memory usage

2. **Slow Response Times:**
   - Check database performance
   - Monitor CPU usage
   - Check network latency

3. **Connection Refused:**
   - Ensure application is running
   - Check port 8080 is open
   - Verify firewall settings

### **Performance Optimization:**

1. **Database:**
   - Add indexes on frequently queried columns
   - Optimize queries
   - Increase connection pool size

2. **Application:**
   - Enable caching
   - Optimize JVM settings
   - Use connection pooling

3. **JMeter:**
   - Increase heap size: `-Xmx2g`
   - Use distributed testing for high loads
   - Optimize test plan

## 📋 **Test Scenarios**

### **Scenario 1: Normal Load**
- **Users:** 20-50
- **Duration:** 10 minutes
- **Purpose:** Validate normal operation

### **Scenario 2: Peak Load**
- **Users:** 100-200
- **Duration:** 15 minutes
- **Purpose:** Test peak traffic handling

### **Scenario 3: Stress Test**
- **Users:** 300-500
- **Duration:** 30 minutes
- **Purpose:** Find breaking point

### **Scenario 4: Endurance Test**
- **Users:** 50
- **Duration:** 2-4 hours
- **Purpose:** Test long-term stability

## 📊 **Sample Load Test Commands**

```bash
# Quick test (5 minutes)
jmeter -n -t simple-jmeter-test.jmx -l quick-test.jtl

# Medium load test (15 minutes)
jmeter -n -t jmeter-deals-api.jmx -l medium-test.jtl -e -o medium-report/

# Heavy load test (30 minutes)
jmeter -n -t jmeter-deals-api.jmx -l heavy-test.jtl -e -o heavy-report/ -Jusers=100 -Jrampup=120 -Jduration=1800

# Spike test
jmeter -n -t jmeter-deals-api.jmx -l spike-test.jtl -e -o spike-report/ -Jusers=200 -Jrampup=10 -Jduration=300
```

## 🎯 **Success Criteria**

### **Performance Targets:**
- ✅ **Response Time:** < 500ms average
- ✅ **Throughput:** > 50 requests/second
- ✅ **Error Rate:** < 1%
- ✅ **Availability:** > 99.9%

### **Resource Usage:**
- ✅ **CPU Usage:** < 80%
- ✅ **Memory Usage:** < 80%
- ✅ **Database Connections:** < 80% of pool

**Happy Load Testing! 🚀**

