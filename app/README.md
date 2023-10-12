# Take Home Project Notes

## Unstated Requirements
The assignment doesn't make clear how to handle some possible, real life situations:

### Can the rating be negative?  
And if so should something like -0.25 be rounded to 0.0 or (more likely) -0.50?

### In real life there is always the possibility that a product coming from the server may contain malformed data--for example, an empty name.
What should we do in such a case?  One possible way to handle bad product items is to exclude them from the list (on the theory that we should never  advertise malformed product information) but log a warning to some kind of error server from whence  a dashboard and an alert could be activated.  In a real life the API's data may be far from "pure".

### Similarly, we aren't told whether all products have ratings.
A missing rating can be defaulted to 0.0,  but that gives the erroneous impression that the product was rated badly.  In real life, the number of  ratings would typically be provided along with the rating number, to clarify this situation.

### If the network fails, should we retry it a few times, perhaps with exponential back-off?
In case of any operational error, what kind of error information should we display in the product page?

## Implementation Improvements
Gson is inherently prone to mishandling fields whose JSON is null.  It will set the field to null in that  case regardless of the fact that that the data class may be non-nullable per Kotlin.  This will cause a later  runtime error, almost surely.  The current implementation guards against this by parsing the  JSON into a "bean" class whose non-intrinsic fields are all nullable.  A better solution might be to  use a more modern JSON library--one that respects the kotlin nullability of the receiving field.
