> [!IMPORTANT]
> THIS REPOSITORY IS DEPRECATED.
>
> Development on this webservice has been moved to: [https://github.com/identifiers-org/monorepo](https://github.com/identifiers-org/monorepo/tree/master/webservices/resolver).
> This new repository targets easier code integration and facilitated CI/CD pipeline. For any concerns, please [contact us](https://docs.identifiers.org/pages/contact) or open a issue at our [main issue board](https://github.com/identifiers-org/identifiers-org.github.io/issues).


# Overview
This Web Service implements [__identifiers.org__](http://identifiers.org) Compact ID resolution API for programmatic access to _ID resolution services_.


# Running the Compact ID Resolution service
The only requirement for running the _resolution service_ is having a working installation of [Docker](http://docker.com).

The docker image is called **identifiersorg/cloud-ws-resolver**, and it is part of [identifiers.org Docker Hub](https://hub.docker.com/r/identifiersorg/).

The following command is an example on how to launch the _resolution service_

> docker-compose -f docker-compose-standalone.yml up


# How to Query the Compact ID Resolution Service
There is a Java based library, [libapi](https://github.com/identifiers-org/cloud-libapi), that implements a client for this Web
Service.

Please, refer to its documentation on how to connect to [identifiers.org](https://identifiers.org) Web Services.


# Contributions
Documentation for developers will be coming soon, through the repository Wiki.


### Contact
Manuel Bernal Llinares
