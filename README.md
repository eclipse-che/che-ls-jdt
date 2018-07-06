# che-ls-jdt
che-ls-jdt

### Build and package into an image
To build and package this into an image run `mvn (your commands) docker:build`. It will then have an image name of che-jdt-ls/java-ls tagged with latest.

### To use the resulting image in a sidecar:
Follow [the instructions](https://www.eclipse.org/che/docs/6/che/docs/language-servers.html#ls-sidecars) with three exceptions.

    1. Change the custom recipe to include che-jdt-ls/java-ls as the image

    2. Set the id in the workspace config as "org.eclipse.che.plugin.java.languageserver"

    3. Mount /home/user/jdtls as a volume for the java-machine-ls

**If you want to use your local docker registry set CHE_DOCKER_ALWAYS__PULL__IMAGE=false when starting Che**
