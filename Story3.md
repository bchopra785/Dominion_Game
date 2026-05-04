Story 3: Cloud Deployment of Player
Server
In this story, you will package your player server in a Docker container, and deploy it to Azure
as an Azure Container App.
Story 3 deliverables
1. A Dockerfile which packages your network player code into a Docker image
2. A URL at which your network player is publicly available
Resources & Tutorials
Docker
1. Docker extension for VSCode (optional, but recommended)
2. Spring Boot with Docker
3. Dockerfile reference
To build an image, use a "docker build ..." command. If you are building on a laptop with
Apple Silicon (an M1/2/3 chip), you will need to add "--platform linux/amd64" to your "docker
build ..." command.
To run a container based on the image, use a "docker run ..." command
To test a running image, use curl to send requests to its HTTP endpoints. You can also try
running your Engine with a NetworkPlayer, where the NetworkPlayer tries to connect to the
server running in the Docker container. When testing everything locally, the decision endpoint
address will be "localhost:<port>/decide".
To debug a running container, run this command from your terminal to get a bash prompt
inside the container: docker run --rm -it -p 3000:3000/tcp <your image
name>:latest /bin/bash
Azure account setup
Only one team member should do this part
1. Activate your Azure Student Account. Follow the link, click "Start free", and enter your
Brandeis email address. You should not need to enter any payment information.
2. Navigate to "Subscriptions" in the Azure Portal, and select your subscription.

Updated 12/4/25

3. Click on "Access Control (IAM)" on the left, then "+ Add", then "Add role assignment"
at the top:

4. Click on "Privileged Administrator Roles", then select "Contributor", then "Next" at the
bottom.
5. Leave "User, group, or service principal" checked, then click "Select members".
6. Add each of your teammates, the instructor, and your group's TA by email.
7. Click "Review + assign" at the bottom.
8. Ask your teammates to try to log into the Azure Portal, to ensure they have access to
your subscription.
Pushing a Docker image to the Azure Container Registry
1. Create an Azure Container Registry
2. Enable an admin user on your registry (see https://stackoverflow.com/a/78834237)
3. Push your built image to the container registry using the "docker push ..." command
a. If you navigate to your new registry and choose the "push a container" button in
the main section of the screen, it will display some sample commands and
instructions.

Deploying a Docker image as an Azure Container App
1. Deploy a new Container App based on the image you just pushed
a. Most options are either self-explanatory or can be left as-is, but here are a few
tips:
b. Don't use a "quickstart image" on the "Container" setup step
c. Enable "Ingress" on the "Ingress" setup step
i. Set it to "Accept traffic from anywhere"
ii. Check "allow insecure connections"

Updated 12/4/25

iii. Enter the port your application listens on (probably 8080, unless you've
changed defaults) in "Target port"

2. After deploying, click “Go to Resource”, then test it by sending a request to the
"Application URL" for your app:

3. Test it for real by running your Engine, and using a Network Player pointed at the URL
for your deployed HTTP server.
Updating an Azure Container App
Updating an Azure Container App is easy, but requires you to use explicit docker tags on your
images, rather than reusing the default "latest" tag.
To update:
1. Rebuild your docker container, but add a tag (e.g. ":v1.1.0") to your image name. Use a
new tag each time you make a change.
2. Push the new image to your container registry
3. Go to the "Application->Containers" menu in your Container App in the Azure Portal.
4. Modify the container to use your newly pushed tag, then click "Save as new revision"