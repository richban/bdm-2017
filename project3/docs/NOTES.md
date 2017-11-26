# Storage Layer

Before digging into the technical details of the storage layer, we concluded the most important goals of our architecture. We had to engineer that our master data set is absolutely corruption proof and fault tolerant which is the most essential part of the Lambda Architecture. The main components of the master dataset: the data model and how data is physically stored. We started by learning the key properties of data.

The data represents trips…

Data comes from the operations systems in xml formats on daily basis. When design our Big Data service, we wanted to ensure that we will be able to answer as many question as possible. Therefore we are storing the rawest data. Storing raw data allow us to maximize our ability to gain new insights, which were not considered when  examining the dataset. We understand that raw data entails more storage requirements and more resources but our service is design with the big data technologies (Hadoop Stack) which are able to manage large amount of data in a distributed, scalable manner.

```
    <timestep time="1.00">
        <vehicle id="0" x="12.537488" y="55.663405" angle="-18.966962" type="DEFAULT_VEHTYPE" speed="1.673759" pos="6.773759" lane="27409327_0" slope="0.000000"/>
 
```

There are number of different possibilities how to represent data within the master dataset. Given our limited time and resources we have chosen for simplicity to store data as we receive it originally in structure XML format. However we recommend the fact-based model, which is similar to datawarehouse models, where the data is deconstructed into units called facts. Facts are atomic, they can’t be subdivided further. If we have chosen a fact based mode it might look like this:


```
union Person {
    1: required PersonID id;
}

union PersonPropertyValue {
    1: 
}

union Vehicle {
    1: required Vehicle id;
}

```

It provides simple expressive representation of data. With a fact-based model, we would have gain numerous advantages. For instance it supports the evolution of the kinds of data stored in the master dataset, because the data from the source may change over the time. This is a crucial property, it would have add whole new capabilities to our system, because the “business” requirements  might change and we would need to add new kinds of data and handle this changes effortlessly as possible.

One of the key advantages to represent data in the master dataset using schemas we experienced when implementing the storage layer. The goal was to easily and effectively manage large (growing) dataset. First we have decided for simplicity to use the low-level HDFS API to manage our storage layer. Although we haven’t done any denormalization for performance issues we exploited the files and folders abstraction to improve storage of the master dataset with vertical partitioning. Vertical partitioning enabled large performance gains, we can access data which is relevant to the computation, therefore we don’t need to access the entire dataset. Vertical partitioning of data is done by sorting the data into separate folders in the distributed file system.

![alt text](./static/vertical_partitioning.jpg "Vertical Partitioning")

We vertically partition the data by day, we create a separate folder for each day of data. Each day a cron job is downloading the data from the source system and afterwards a Scala script is scheduled which appends the new data to hdfs. The Scala script is just using the files-and-folders API of hdfs, thus easily certain bugs and mistakes can break the vertical partitioning constraints of a dataset. This indicated that using hdfs API directly is too low-level of an abstraction for such a task like vertical partitioning in production. All kinds of operations and checks need to happen to correctly manage the dataset thus we have moved to higher-level abstraction solutions which are not only scalable, fault-tolerant and performant, but elegant as well are a necessity.

An alternative solution to our problem have been solved with a library called Pail (dfs-datastores). Pail keeps metadata about the dataset. By using metadata, Pail abstracts the low-level API and allow us to significantly improve the management of a dataset without worrying about violating it’s integrity.

That concludes the storage layer. Moving towards higher abstraction libraries like Pail we could easily manage dataset on a distributed filesystem, while isolating from the details of the filesystem and violations of the dataset integrity.

![alt text](./static/storage_layer_pipe.jpg "Schema of Storage Pipe-line")

# Batch Layer


## Motivation (Views)

* view 1
* view 2
* view 3

## Batch Procesing

* Flattening Explained (Pipe Diagram)
* Normalization Explained (Pipe Diagram)
* Recomputation Explained
* Human Fault Tolerance
* Pipe Diagrams of Views

## Serving Layer

* Pipeline
* How we store the views
* Hive


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


