# InfluxExporter
Export Influx data by template and time periods
Works with Influx v1.8 using Java 8


### Arguments
```bash
***-config***		Path to config file  
***-out***			Path to output data  
***-debug***		More logs  
***-times***		Start of time periods, from 1 to N  
***-durations***	Periods durations, could be 1 or N  
***-profiles***		Profiles in %, could be 1 or N  
***-name***			Output xlsx file name  
```

### Start example
```java
java -jar InfluxExporter-1.0.jar -config C:\example\config.txt -out C:\example\out -debug true -times 2021-03-18T18:30:00 2021-03-18T19:00:00 -durations 01:00:00 00:30:00 -profiles 100 150.5 -name example1
```  
### Config example
```python
#connection data  
influx.url=http://localhost:8086  
influx.user=user  
influx.password=pass  
influx.database=dbname  

#xlsx data  
xlsx.template_path=C:\\example\\template.xlsx  

#sql data  
sql.count=2  
\#sql query 1  
sql1.query=SELECT count("responseTime"), percentile("responseTime", 99.9) FROM "dbname"."autogen"."transaction" WHERE "status"='0' and time > '__start__' AND time < '__finish__' GROUP BY "name","status"  
\#sheet name for sql query 1  
sql1.sheet=raw1  

#sql query 2  
sql2.query=SELECT count("responseTime"), percentile("responseTime", 99.9) FROM "dbname"."autogen"."transaction" WHERE "status"!='0' and  time > '__start__' AND time < '__finish__' GROUP BY "name","status"  
\#sheet name for sql query 2  
sql2.sheet=raw2  
```
