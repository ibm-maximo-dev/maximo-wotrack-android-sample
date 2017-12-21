# Maximo work order tracking Android sample application

This simple Android Kotlin application demonstrates how easily developers and business partners can build their own solutions by leveraging the Maximo REST APIs. This tutorial shows how to use these APIs and provides instructions for building and testing developed applications.

This sample application includes the following set of features:

## Login / Logout
This feature allows the users to login by using the built-in authentication mechanisms.
The login operation is the first step that any developer must complete to use the Maximo REST APIs.
Users must be authenticated and have the required permissions to perform these operations.

You must have an active Maximo user account to use these APIs.

After this method is called, it maintains a reference to a Maximo user profile that is used to determine whether the logged user is authorized to perform the requested operations.

The following example illustrates how to use this API to authenticate a user:

```kotlin
var options = Options().user(userName).password(password).auth("maxauth")
options = options.host(host).port(port).lean(true)
var connector = MaximoConnector(options).debug(true)
connector.connect()
```

It is strongly advised that you keep only a single instance of the MaximoConnector object on your application, given that it is a stateful object that keeps a reference to the logged Maximo user profile. It is not only a good practice, but it can save you time and resources from having to re-authenticate every time you invoke another method from the APIs.

To use this method, you need to build an Options object by supplying the following values:
 - User and password credentials
 - Hostname or IP address
 - Port number

Connection or authentication failures can be handled by catching the following exception types: IOException and OslcException.

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
This feature allows the users to list all work orders that are in the Waiting for Approval status and are visible to their organization and Site.
This API method provides a set of input parameters that the users can supply to select a collection of work orders records.

The following code sample shows how to select a set of work order records by using options like pagination, sorting, and filtering:

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

Developers who built solutions for Maximo Asset Management in the past might notice that the Maximo REST API classes Resource and ResourceSet are similar to the Mbo/MboSet pair that is available in the Maximo business object framework.

By using an instance of the MaximoConnector class, you can fetch a ResourceSet object for any of the object structures that are published in the Maximo REST API.

The following example shows how to obtain an instance of the ResourceSet class for the MXWO object structure that holds the work order records information.
 ```kotlin
    val workOrderSet = connector.resourceSet("mxwo") // This returns a ResourceSet object instance
 ```
After you hold an instance of the ResourseSet class, you can perform actions like searching for existing records, ordering records by a specific set of columns, fetching a records page, and more.

The following list shows the most commonly used actions and input parameters that can be provided to select work order records:
 - fetch(): Fetches a set of records according to the input parameters that are provided.
 - load(): Loads a set of records according to the input parameters that are provided.
 - count(): Returns the count for the current number of records that are loaded into this set.
 - totalCount(): Returns the total count (remote) for all records that are persisted for this set (Object Structure).
 - nextPage(): Fetches the next page of records for this set.
 - previousPage(): Fetches the previous page of records for this set.
 - member(value : Int): Returns an element that was previously loaded into this set by using the specified index position.
 ```kotlin
     workOrderSet.fetch()
     workOrderSet.load()
     var count = workOrderSet.count()
     var totalCount = workOrderSet.totalCount()
     workOrderSet.nextPage()
     workOrderSet.previousPage()
     var resourceObject = workOrderSet.member(0)
 ```
 - oslc.select: A String var-args parameter that allows the user to fetch a set of properties for the selected objects instead of loading all their properties. This parameter is useful for applications that are developed for environments that have small memory footprints.
 ```kotlin
     workOrderSet.select("spi:wonum", "spi:description", "spi:status")
 ```
 - oslc.where: A String parameter in the OSLC query syntax format (SQL-based) that allows the user to define a where clause to filter the record set.
  ```kotlin
     workOrderSet.where("spi:istask=0 and spi:status='WAPPR'")
 ```
 - oslc.paging: A flag that enables or disables paging for the selected record set.
  ```kotlin
     workOrderSet.paging(true)
 ```
 - oslc.pageSize: An integer parameter that defines the page size for the selected record set.
  ```kotlin
     workOrderSet.pageSize(5)
 ```
 - oslc.orderBy: A String var-args parameter that allows the user to define a set the properties that are used to sort the obtained record set.
  ```kotlin
     workOrderSet.orderBy("spi:wonum")
 ```
 - oslc.searchTerms: A String var-args parameter that performs record-wide text searchs for the tokens that are specified.
  ```kotlin
     workOrderSet.searchTerms("pump", "broken")
 ```

After these elements are successfully loaded into the ResourceSet, they must be convered into a friendly data format that is usable inside the application context. That's when JSON objects are used.
 ```kotlin
     val resourceObject = workOrderSet.member(0) // I am a Resource object
     val jsonObject = resourceObject.toJSON() // I am a JSON object, much more friendly and human-readable.
 ```
The Resource class is simply a data object representation of an object structure. It provides several utility methods to update, merge, add or even delete an Object Structure. It also provides methods to allow conversions to other data types like: JSON or byte arrays. In the previous example, after a previously loaded Resource object is fetched, it is converted to its JSON object representation.

## Update a Work Order

## Create a new Work Order
