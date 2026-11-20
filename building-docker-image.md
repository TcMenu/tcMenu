## Building and preparing the source

Firstly, before using the docker file make sure you have build the `tcmenugen` node application and also run a full maven build **of a release version**. Once you have the version you'd like to build checked out, follow the guide below. You will need the latest LTS Java JDK, maven, and npm. 

Assuming you are in the `tcMenu/web-designer` directory, change into the `tcmenugen` directory to build the web UI.

    npm install
    npm run build

Next make sure that the right Java JDK is on the path, and that `$JAVA_HOME` is set to that JDK directory. Change back into the `tcMenu/web-designer` directory and run: 

    mvn clean install

If any tests fail on your machine (really they should not) then you can run with `-DskipTests`. At this point you have everything build that is needed to make a docker image.

## Building and preparing the image

To build the docker image, you should be in the `tcMenu` repository root directory.

If you have any previous version running, stop that first.

    docker stop $CONTAINER_NAME 2>$null
    docker rm $CONTAINER_NAME 2>$null

Now build the image

    docker build --pull -t 'tcmenu-web-generator:latest' -f Dockerfile .

You need a local file somewhere called `designer-versions.json` that contains the versions of the designer 
you want to host. You can locate this file wherever you like and mount it into the container. The minimum content of the
`designer-versions.json` file is an empty array:

```
[]
```

Otherwise, for example:

```
[
  {
    "subdomain": "designer",
    "description": "Stable Designer",
    "versionPattern": "4.5.x",
    "url": "https://designer.thecoderscorner.com"
  },
  {
    "subdomain": "designer-next",
    "description": "Latest Designer",
    "versionPattern": "5.0.x",
    "url": "https://designer-next.thecoderscorner.com"
  }
]
```

Then run docker

    docker run -d --mount type=bind,src=<path-to>/designer-versions.json,dst=/mnt/designer-versions.json --name tcmenu-web-service -p "8080:8080" --restart unless-stopped `"tcmenu-web-generator:latest"


Container should now be started on http://localhost:8080
