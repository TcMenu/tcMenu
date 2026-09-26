## First steps

Firstly, before using the docker file make sure you have build the `tcmenugen` node application and also run a full maven build **of a release version**. 

## Building and preparing the image

If you have any previous version running, stop that first.

    docker stop $CONTAINER_NAME 2>$null
    docker rm $CONTAINER_NAME 2>$null

Now build the image

    docker build -t "tcmenu-web-generator:latest" -f Dockerfile .

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

    docker run -d --mount --type=bind,src=/<designer-versions.json>,dst=/mnt/designer-versions.json --name tcmenu-web-service -p "8080:8080" --restart unless-stopped `"tcmenu-web-generator:latest"


Container should now be started on http://localhost:8080 ==="
