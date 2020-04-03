# Roadmap

This document contains the roadmap for the development of the saker.nest repository. It is related only to repository features and doesn't include information for other packages.

This document contains the proposed features, improvements, and crucial bugfixes for the saker.build system. The targeted goals doesn't specify a release date, as due to the limited maintainer development resources, we currently can't estimate exact release dates.

The specified features, improvement and bugfixes will ship as they are implemented. Progress can bee seen in the associated GitHub issues. The priority of a goal is determined on a case-by-case basis.

See also: [saker.build roadmap](root:/saker.build/doc/roadmap.html)

#### Remote bundle storage

A bundle storage type should be introduced that makes the bundles accessible through the network. Its goal is to be able to share bundles for a given organization, for a restricted scope.

The bundles of the remote storage are accessible over RMI. The clients can connect to the server, and query the bundles for their use-case. The goal is to configure the build execution in an unified way for a set of computers.

The storage can run as server via repository actions. Accessing the storage is simply configuring the build execution that connect to the given remote storage.

The server may enable the clients to publish for them. Some way of access management should be implemented for it.

#### Server namespaces

Users should be able to define private, shared, and public namespaces for their packages. The API server of the saker.nest repository should support these namespaces. The [](#remote-bundle-storage) goal takes priority over this one, as that could be used as a self-hosted workaround for private namespaces.
