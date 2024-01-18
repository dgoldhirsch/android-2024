# 2024 Android Scaffold App

A sample app using a reasonable tech stack containing the following elements:

* Jetpack Compose
* Simple, uni-directional architecture implemented with ViewModel, StateFlow
* Repository and Data Source architecture
* Coroutines/Flow
* Hilt

## Architecture
This is a unidirectional (reactive) app implemented with a view model that maintains a _ui state_ object that is collected-as-state by the Compose main layout.
There are really two state machines in this app:  the true state of the application is maintained within the view model;  and, a subset of that--the _ui state_--is monitored by the Compose layout.

The repository is modelled to return a _response_ that if successful contains safe data models that be used by the view model to drive the state of the app.  The repository hides all of the mapping and null handling.

Currently the app consists of a single page populated from a single repository, so, it's all very simple.

