# Data Quality

Preview of merged dataset (Wifi Data & Meta Data)

```
+--------------------+--------------------+---------+-----+-------+---------+----------+--------------+----------+----------+--------+--------------------+
|                 did|                 cid| clientOS| rssi|snRatio|     ssid|        ts|deviceFunction|deviceMode|deviceName|location|              upTime|
+--------------------+--------------------+---------+-----+-------+---------+----------+--------------+----------+----------+--------+--------------------+
|d2a17aa6f2e62a490...|981916a959a0ec948...|Apple iOS|-65.0|   30.0|ITU-guest|1507170845|            AP|    Portal| AH-0324c0|    5A10|19 Days, 14 Hrs 5...|
|d2a17aa6f2e62a490...|981916a959a0ec948...|Apple iOS|-54.0|   41.0|ITU-guest|1507171028|            AP|    Portal| AH-0324c0|    5A10|19 Days, 14 Hrs 5...|
|d2a17aa6f2e62a490...|981916a959a0ec948...|Apple iOS|-58.0|   37.0|ITU-guest|1507172494|            AP|    Portal| AH-0324c0|    5A10|19 Days, 14 Hrs 5...|
|d2a17aa6f2e62a490...|981916a959a0ec948...|Apple iOS|-58.0|   37.0|ITU-guest|1507172555|            AP|    Portal| AH-0324c0|    5A10|19 Days, 14 Hrs 5...|
|d2a17aa6f2e62a490...|981916a959a0ec948...|Apple iOS|-57.0|   38.0|ITU-guest|1507172616|            AP|    Portal| AH-0324c0|    5A10|19 Days, 14 Hrs 5...|
|d2a17aa6f2e62a490...|981916a959a0ec948...|Apple iOS|-57.0|   38.0|ITU-guest|1507172677|            AP|    Portal| AH-0324c0|    5A10|19 Days, 14 Hrs 5...|
|d2a17aa6f2e62a490...|981916a959a0ec948...|Apple iOS|-56.0|   39.0|ITU-guest|1507172738|            AP|    Portal| AH-0324c0|    5A10|19 Days, 14 Hrs 5...|
|d2a17aa6f2e62a490...|981916a959a0ec948...|Apple iOS|-51.0|   44.0|ITU-guest|1507172860|            AP|    Portal| AH-0324c0|    5A10|19 Days, 14 Hrs 5...|
|d2a17aa6f2e62a490...|981916a959a0ec948...|Apple iOS|-55.0|   40.0|ITU-guest|1507172982|            AP|    Portal| AH-0324c0|    5A10|19 Days, 14 Hrs 5...|
|d2a17aa6f2e62a490...|981916a959a0ec948...|Apple iOS|-55.0|   40.0|ITU-guest|1507173043|            AP|    Portal| AH-0324c0|    5A10|19 Days, 14 Hrs 5...|
+--------------------+--------------------+---------+-----+-------+---------+----------+--------------+----------+----------+--------+--------------------+
```


Number of rows in the data set

```bash
+----------+
|count(did)|
+----------+
|   8239260|
+----------+
```

Schma

```bash
root
 |-- did: string (nullable = true)
     |-- cid: string (nullable = true)
     |-- clientOS: string (nullable = true)
     |-- rssi: double (nullable = false)
     |-- snRatio: double (nullable = false)
     |-- ssid: string (nullable = true)
     |-- ts: long (nullable = false)
     |-- deviceFunction: string (nullable = true)
     |-- deviceMode: string (nullable = true)
     |-- deviceName: string (nullable = true)
     |-- location: string (nullable = true)
     |-- upTime: string (nullable = true)
    complcache_start_auto_complete)
```

Distinct Clients

* Update the null and empty data with undefined value or unknown

```sql
spark.sql("SELECT coalesce(nullif(trim(clientOS), ''), 'unknown'), count(clientOS)  FROM time_series_location GROUP BY 1")
```

```bash
+--------------------+-------+
|            clientOS|  count|
+--------------------+-------+
|                CrOS|    828|
|          Windows 10|1052844|
|             unknown| 247177|
|            Slingbox|      1|
|                null|      0|
|               Linux|  33923|
|           Apple iOS|1365351|
|     Windows 7/Vista| 752960|
|Panasonic G20 Tel...|      2|
|             Android|1008223|
|                    | 247450|
|        Windows 8/10|  68247|
|            Mac OS X|2120660|
+--------------------+-------+
```

Distinct ssid

```bash
scala> spark.sql("SELECT coalesce(nullif(trim(ssid), ''), 'unknown'), count(ssid)  FROM time_series_location GROUP BY 1").show()
+----------------------------------------------------------------+-----------+
|coalesce(nullif(trim(time_series_location.`ssid`), ''), unknown)|count(ssid)|
+----------------------------------------------------------------+-----------+
|                                                         eduroam|    3190570|
|                                                         unknown|          0|
|                                              Game-AI-Conference|      58417|
|                                                           ITU++|    3088480|
|                                                  ITU-Conference|       7792|
|                                                             5te|      63182|
|                                                       ITU-guest|     294730|
|                                                         sensors|     199326|
+----------------------------------------------------------------+-----------+
```
