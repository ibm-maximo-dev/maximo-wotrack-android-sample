# Android Sample Maximo Work Order Tracking Application

This is a very simple Android Kotlin application that has been developed in order to demonstrate how easily developers and business partners can build their own solutions by leveraging the Maximo REST APIs that are available in the Maximo Platform. This tutorial will guide you on how to use those APIs, and will provide instructions for building and testing the applications.

This sample application is composed by the following set of features that will be exhibited in further detail:

## Login / Logout
Allows the users to login using Maximo built-in authentication mechanisms.
The login operation is the first step that any developer must go through in order to use the Maximo REST APIs.
In order to allow the use of other features available on it's APIs, Maximo requires users to be both authenticated and authorized to perform these operations.

Hence to use these APIs, you need to have an active Maximo user account as a primary condition.

Once this method is called it maintains a reference to a Maximo User profile that is used to determine whether the logged user is authorized to perform the requested operations.

```
options = Options().user(userName).password(password).auth("maxauth")
options = options.host(host).port(port).lean(true)
connector = MaximoConnector(options).debug(true)
connector.connect()
```

## List Work Orders that are Waiting Approval

## Show basic Work Order details

## Edit Work Order Details

## Approve a Work Order

## Create a new Work Order
