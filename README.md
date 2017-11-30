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
This API method provides a set of input parameters that the users may provide to select a collection of Work Orders records.

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
This is a list of the input parameters that may be provided to select Work Order records:
 - oslc.select: Allow the user to fetch a set of properties for the selected objects, instead of loading all their properties. This is aimed for applications that are developed for environments with small memory footprints.
 ```kotlin
     workOrderSet.select("spi:wonum", "spi:description", "spi:status")
 ```
 - oslc.where: Allow the user to define a where clause to filter record set.
  ```kotlin
     workOrderSet.where("spi:istask=0 and spi:status='WAPPR'")
 ```
 - oslc.paging: Enable/disable paging for the selected record set.
  ```kotlin
     workOrderSet.paging(true)
 ```
 - oslc.pageSize: Set the page size for the selected record set.
  ```kotlin
     workOrderSet.pageSize(5)
 ```
 - oslc.orderBy: Allow the user to define a set the properties used to sort the obtained record set.
  ```kotlin
     workOrderSet.orderBy("spi:wonum")
 ```
 - oslc.searchTerms: Record wide text search for tokens specified as this query parameter value.
  ```kotlin
     workOrderSet.searchTerms("pump")
 ```

## Show basic Work Order details

## Edit Work Order Details

## Approve a Work Order

## Create a new Work Order
