### LoadXML

```
scala> xml.printSchema
root
 |-- time: double (nullable = true)
 |-- person: array (nullable = true)
 |    |-- element: struct (containsNull = true)
 |    |    |-- _VALUE: string (nullable = true)
 |    |    |-- _angle: double (nullable = true)
 |    |    |-- _edge: string (nullable = true)
 |    |    |-- _id: long (nullable = true)
 |    |    |-- _pos: double (nullable = true)
 |    |    |-- _slope: double (nullable = true)
 |    |    |-- _speed: double (nullable = true)
 |    |    |-- _x: double (nullable = true)
 |    |    |-- _y: double (nullable = true)
 |-- vehicle: array (nullable = true)
 |    |-- element: struct (containsNull = true)
 |    |    |-- _VALUE: string (nullable = true)
 |    |    |-- _angle: double (nullable = true)
 |    |    |-- _id: long (nullable = true)
 |    |    |-- _lane: string (nullable = true)
 |    |    |-- _pos: double (nullable = true)
 |    |    |-- _slope: double (nullable = true)
 |    |    |-- _speed: double (nullable = true)
 |    |    |-- _type: string (nullable = true)
 |    |    |-- _x: double (nullable = true)
 |    |    |-- _y: double (nullable = true)
 ```


