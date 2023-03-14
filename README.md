# CoAP-binding
CoAP binding for openHAB. Is heavily based on HTTPbinding (see description)


!_______IMPORTANT WARNING_______!:

This is a part of a study project which to the date might contain errors. The files which are presented here are mostly a modified version of HTTP-binding that can be found here:

https://github.com/openhab/openhab-addons/tree/main/bundles/org.openhab.binding.http

The authorship of the developers who created the original binding that was used as the basis for the project is preserved in the files, as is information about the original license.

What was tried to do in short:
- rewrite all responses and requests and their processing into CoaP implementation using Californium;
- change configuration where needed and structure of classes which represent the request/response content or somehow relate to it.

Current state of the work:
- there are still some places that make me feel insecure about my changes and which are higly likely implemented wrong;
- there are some casts between object that can be illegal (but allowed by the compiler) and would not work in the live environment as expected.


