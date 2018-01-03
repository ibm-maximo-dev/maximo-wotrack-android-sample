# Maximo Work Order Tracking Android Sample Application

This is a very simple Android Kotlin application that has been developed in order to demonstrate how easily developers and business partners can build their own solutions by leveraging the Maximo REST APIs that are available in the Maximo Platform. This tutorial will guide you on how to use these APIs, and will provide instructions for building and testing developed applications.

This sample application is composed by the following set of features that will be exhibited in further detail:

## Login / Logout
Allows the users to login using Maximo built-in authentication mechanisms.
The login operation is the first step that any developer must go through in order to use the Maximo REST APIs.
In order to allow the use of other features available on it's APIs, Maximo requires users to be authenticated and to hold the required permissions to perform these operations.

Hence as a pre-condition you need to have an active Maximo user account to use these APIs.

Once this method is called it maintains a reference to a Maximo User profile that is used to determine whether the logged user is authorized to perform the requested operations.

This is a simple example that illustrates how to use this API to authenticate a user:

```kotlin
var options = Options().user(userName).password(password).auth("maxauth")
options = options.host(host).port(port).lean(true)
var connector = MaximoConnector(options).debug(true)
connector.connect()
```

It is strongly advised that you keep only a single instance of the MaximoConnector object on your application, given that this is a stateful (retains state across multiple invokations) object that keeps a reference to the logged Maximo user profile. It is not only a good practice, but it is going to save you time and resources from having to re-authenticate every time you need to invoke another method from the APIs.

To use this method you need to build an Options object by supplying the following values:
 - User and password credentials
 - Hostname or IP
 - Port number

Connection or authentication failures should be handled by catching the following exception types: IOException and OslcException.

```kotlin
try {
    ...
    connector.connect()
} catch (e: IOException) {
    // Handle connection failures here...
} catch (e: OslcException) {
    // Handle authentication failures here...
}
```
## List Work Orders that are Waiting for Approval
This feature allows the users to list all Work Orders which are on the "Waiting for Approval" status and are visible to their Organization and Site.
This API method provides a set of input parameters that the users may supply in order to select a collection of Work Orders records.

This is a code sample that shows how to select a set of Work Order records using options like pagination, sorting, and filtering:

```kotlin
var PAGE_SIZE = 5
// "mxwo" is the Object Structure defined to represent a Work Order object.
val workOrderSet = connector.resourceSet("mxwo") // This returns a ResourceSet object instance
val resultList = mutableListOf<JsonObject>() // Creates an empty list to hold JsonObject instances.
workOrderSet.paging(true) // Enable pagination.
workOrderSet.pageSize(PAGE_SIZE) // Set the page size.
// Use OSLC query syntax to skip tasks and only fetch Work Orders that are "Waiting for Approval"
workOrderSet.where("spi:istask=0 and spi:status='WAPPR'")
workOrderSet.orderBy("spi:wonum") // Ordering by Work Order Number
workOrderSet.fetch()
var i = 0
while (i.toInt() < PAGE_SIZE) {
    val resource = workOrderSet.member(i) // Return a Resource instance
    i = i.inc()
    val jsonObject = resource.toJSON() // Convert a Resource to a JsonObject representation
    resultList.add(jsonObject) // Add retrieved JsonObject instance to the list
}
```
### ResourceSet Component
So in order to understand how this feature works, we need to explain a few steps.
Developers that have built solutions for the Maximo Platform before will find the Maximo REST API classes Resource and ResourceSet to be very similar to the Mbo/MboSet pair available in the Maximo MBO framework.

This was intentionally designed to make it as simples as possible for a Maximo developer to build/integrate solutions with Maximo by using these remote APIs from external applications.

Hence, using an instance of the MaximoConnector class, you may fetch a ResourceSet object for any of the Object Structures that are published on the Maximo REST API.

In the following example, we obtain an instance of the ResourceSet class for the MXWO object structure which holds Work Order records information.
 ```kotlin
    val workOrderSet = connector.resourceSet("mxwo") // This returns a ResourceSet object instance
 ```
Once you hold an instance of the ResourseSet class, you may actually perform actions like searching for existing records,  ordering records by a specific set of columns, fetching a records page and so much more.

This is a list of the most commonly used actions and input parameters that may be provided to select Work Order records:
 - fetch(): Fetches a set of records according to the input parameters provided.
 - load(): Loads a set of records according to the input parameters provided.
 - count(): Returns the count for the current number of records loaded into this set.
 - totalCount(): Returns the total count (remote) for all records persisted for this set (Object Structure).
 - nextPage(): Fetches the next page of records for this set.
 - previousPage(): Fetches the previous page of records for this set.
 - member(value : Int): Returns an element previously loaded into this set, using the specified index position.
 ```kotlin
     workOrderSet.fetch()
     workOrderSet.load()
     var count = workOrderSet.count()
     var totalCount = workOrderSet.totalCount()
     workOrderSet.nextPage()
     workOrderSet.previousPage()
     var resourceObject = workOrderSet.member(0)
 ```
 - oslc.select: This is a String var-args parameter that allows the user to fetch a set of properties for the selected objects, instead of loading all their properties. This is aimed for applications that are developed for environments with small memory footprints.
 ```kotlin
     workOrderSet.select("spi:wonum", "spi:description", "spi:status")
 ```
 - oslc.where: This is a String parameter in OSLC query syntax format (SQL-based) that allows the user to define a where clause to filter thhe record set.
  ```kotlin
     workOrderSet.where("spi:istask=0 and spi:status='WAPPR'")
 ```
 - oslc.paging: This is a flag that enables/disables paging for the selected record set.
  ```kotlin
     workOrderSet.paging(true)
 ```
 - oslc.pageSize: Integer parameter that defines the page size for the selected record set.
  ```kotlin
     workOrderSet.pageSize(5)
 ```
 - oslc.orderBy: This is a String var-args parameter that allows the user to define a set the properties used to sort the obtained record set.
  ```kotlin
     workOrderSet.orderBy("spi:wonum")
 ```
 - oslc.searchTerms: Another String var-args parameter that performs record wide text searchs for tokens specified.
  ```kotlin
     workOrderSet.searchTerms("pump", "broken")
 ```

So after successfully loading these elements into the ResourceSet, we need to convert them into a friendly data format that is usable inside the application context. That's when JSON objects come into action.
 ```kotlin
     val resourceObject = workOrderSet.member(0) // I am a Resource object
     val jsonObject = resourceObject.toJSON() // I am a JSON object, much more friendly and human-readable.
 ```
The Resource class is simply a data object representation of an Object Structure. It provides several utility methods to update, merge, add or even delete an Object Structure. It also provides methods to allow conversions to other data types like: JSON or byte arrays. In the previous example, after fetching a previously loaded Resource object, we convert that to it's JSON object representation.

Up to this point, we expect you to be able to list and view data that is provided by the Maximo REST APIs, through the use of the methods exhibited in this tutorial. In the remainder of this tutorial, we aim to demonstrate how to modify and create new persistent data records.

## Create/Update a Work Order
Before we discuss the actual methods available for updating and creating new data records, we need to provide some background information about how these methods actually work.

### JSON

REST APIs usually rely on JSON (JavaScript Object Notation) format to transport data between the client and the server.
Hence, in order to modify or create records, you need to provide a JSON representation of the record you wish to modify or create as an input for the API method.

Building and modifying JSON structures can be easily accomplished by the use of specific APIs, almost every modern programming language provides a set of APIs to build and manipulate JSON. In this tutorial, we exhibit a very simple example of how to build JSON objects in the Android/Kotlin programming language.

 ```kotlin
     var objectBuilder = Json.createObjectBuilder() // This creates a JsonObjectBuilder component.
     objectBuilder.add("wonum", wonum.text.toString()) // Adding "WONUM" attribute to the JSON structure.
     objectBuilder.add("siteid", MaximoAPI.INSTANCE.loggedUser.getString("locationsite")) // Adding "SITEID" attribute.
     objectBuilder.add("orgid", MaximoAPI.INSTANCE.loggedUser.getString("locationorg")) // Adding "ORGID" attribute.
     objectBuilder.build() // This returns a JsonObject instance.
 ```
 
The objectBuilder component works similar to a Map data structure. It holds a key-value pair for every attribute that is added to the Object Builder. After you have finished setting up the attributes, you just need to invoke the build() method and it returns a JsonObject instance that is required for updating/creating records through the Maximo REST APIs.

### Creating a Work Order

### Updating a Work Order
