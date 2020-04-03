# 0.8.0 seems like an odd initial release number

<small>2020 January 6</small>

If you're here, you're probably wondering why 0.8.0 was chosen as the initial version number for saker.build. At the first glimpse, it seems like arbitrary, but let's take a deeper dive.

The saker.build system and related ecosystem sees the first light of day in this release. It became available to the general public and developers can start building their projects using it. We need to keep one thing in mind is that as the initial release, it has not been battle tested by a wide audience. We've been [dogfooding](https://en.wikipedia.org/wiki/Eating_your_own_dog_food) it for years, however, other developers may have different experiences with it.

After the release, other developers may discover missing features or breaking bugs that would be hard to repair in a release that has a stable API and surrounding mechanisms. By releasing with the version number 0.8.0 we wanted to signal that saker.build may still undergo some breaking changes until it is considered fully stable and governed under the [](root:/saker.build/doc/extending/api.html#api-change-policy).

Our plan with the upcoming saker.build releases are the following:

**0.9.0** should fix any encountered core problems with the build system based on user feedback. This release is going to be considered as a release candidate for 1.0.0, while testing the build system further for extensibility and usability.

**1.0.0** is going to be the first stable release that has a strictly managed API policy.

There also may be intermediate release between the above versions (like 0.8.x and 0.9.y). We also reserve the right to go further (0.10.0, 0.11.0, etc...) if we encounter any more core issues.

One can wonder, why haven't we chosen 0.1.0 like [semver.org](https://semver.org/#how-should-i-deal-with-revisions-in-the-0yz-initial-development-phase) suggests. By staring at 0.8.0 we wanted to signal that we consider saker.build to be more mature than some early-beta software. For us, the version 0.1.0 implies that the software is in a very early phase of development, however, that is not the case for saker.build. 
