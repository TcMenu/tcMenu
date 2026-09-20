## First steps

Firstly, before using the docker file make sure you have build the `tcmenugen` node application and also run a full maven build **of a release version**. 

## Building and preparing the image

If you have any previous version running, stop that first.

    docker stop $CONTAINER_NAME 2>$null
    docker rm $CONTAINER_NAME 2>$null

Now build the image

    docker build -t "tcmenu-web-generator:latest" -f Dockerfile .

Then run docker

    docker run -d --name tcmenu-web-service -p "8080:8080" --restart unless-stopped `"tcmenu-web-generator:latest"


Container should now be started on http://localhost:8080 ==="
