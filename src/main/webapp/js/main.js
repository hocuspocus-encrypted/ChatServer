let ws;
let wsRooms;
let chatRoomCodes = [];
const textBox = document.getElementById("log");
let selectedCode;


// Function to generate timestamp
function timestamp() {
    let d = new Date(), minutes = d.getMinutes();
    if (minutes < 10) minutes = '0' + minutes;
    return d.getHours() + ':' + minutes;
}

// Function to fetch chat rooms data and process it
async function fetchDataAndProcess() {
    let wsRooms = ("http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet?create=false");

    const result = await fetch(wsRooms);

    let data = await result.text();

    data = JSON.parse(data)
    let rooms = data.rooms;

    console.log(data);

    // used to populate chatRoomCodes array with room codes
    rooms.forEach(room => {
        chatRoomCodes.push(room);
    });

    updateRoomList()

    alert("Warning!!! You have to select a room before beginning, Just want to let you know. ");
}

fetchDataAndProcess();

console.log('chat',chatRoomCodes)


//Function to update the list of chat room codes
function updateRoomList() {
    const roomsList = document.getElementById("roomsList");
    roomsList.innerHTML = ""; // Clear the existing list

    chatRoomCodes.forEach(code => {
        const listItem = document.createElement("li");
        listItem.id = "list_item";
        const button = document.createElement("button");
        button.id = "room_button";
        button.textContent = code;
        button.addEventListener("click", function() {
            textBox.value = ""; // Clear chat log
            enterRoom(code); // Call enterRoom function with the clicked code
        });
        listItem.appendChild(button);
        roomsList.appendChild(listItem);
    });
}

// Function to make a new room
function newRoom(){
    // calling the ChatServlet to retrieve a new room ID
    let callURL= "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet?create=true";
    fetch(callURL, {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
        },
    })
        .then(response => response.text())
        .then(response => JSON.parse(response))
        .then(response => {
            chatRoomCodes.push(response.sCode); // Add new room codes to chatRoomCodes
            updateRoomList();
            textBox.value = ''; // clear chat log
            enterRoom(response); // enter the room with the code
        });
}

// Function for when a user enters a chatroom
function enterRoom(code)
{
    ws = new WebSocket("ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/" + code);

    // function for incoming messages
    ws.onmessage = function (event) {
        console.log(event.data);
        let message = JSON.parse(event.data);
        document.getElementById("log").value += "[" + timestamp() + "] " + message.message + "\n";
    }

    // function for sending messages
    document.getElementById("input").addEventListener("keyup", function (event) {
        if (event.key === "Enter") {
            let message = event.target.value.trim();
            if(message !== ""){
                let request = {"type": "chat", "msg": event.target.value};
                ws.send(JSON.stringify(request));
                event.target.value = "";
            }
        }
    });
}