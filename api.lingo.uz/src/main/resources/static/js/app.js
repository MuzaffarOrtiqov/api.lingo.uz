let stompClient = null;
const connectBtn = document.getElementById("connect");
const disconnectBtn = document.getElementById("disconnect");
const messageInput = document.getElementById("messageInput");
const sendBtn = document.getElementById("sendBtn");
const token = localStorage.getItem("jwtToken");
const chatArea = document.getElementById("chatArea");
const chatTitle = document.getElementById("chatTitle");
const messages = document.getElementById("messages");
const userDetail = localStorage.getItem("userDetail");
const currentProfileId = JSON.parse(userDetail).id;
const nameInput = JSON.parse(userDetail).name;
const chatItems = document.querySelectorAll('.chat-item');

let selectedChatId = null;
let selectedChatType = null;

// init
window.addEventListener("DOMContentLoaded", async () => {

    // test();
    await connect();
    console.log("Connection established");
    //add event listener auto send message
    messageInput.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            const message = event.target.value;
            if (message.trim() !== "") {
                sendMessage();
            }
        } else { // send user typing
            const jwt = localStorage.getItem("jwtToken");
            if (!jwt) {
                window.location = 'login.html';
            }
            stompClient.send("/app/chat.userTyping." + selectedChatId + "." + selectedChatType, {}, null);
        }
    });
    // // scroll pagination
    // chatMessageContainer.addEventListener("scroll", async function () {
    //     if (isLoading) {// if true return back
    //         return;
    //     }
    //
    //     let scrollDirection = 'DOWN';
    //     let lmCreatedDate = null;
    //     let makeRequest = false;
    //     let scrollChild = null;
    //     const first = chatMessageContainer.firstElementChild;
    //     if (first && isElementVisibleInContainer(first)) {
    //         // console.log("First message is fully visible");
    //         scrollDirection = 'UP';
    //         scrollChild = chatMessageContainer.firstElementChild;
    //         lmCreatedDate = scrollChild.dataset.createddate
    //         makeRequest = true;
    //     }
    //     const last = chatMessageContainer.lastElementChild;
    //     if (last && isElementVisibleInContainer(last)) {
    //         // console.log("Last message is fully visible");
    //         scrollChild = chatMessageContainer.lastElementChild;
    //         lmCreatedDate = scrollChild.dataset.createddate
    //         makeRequest = true;
    //     }
    //     if (scrollChild) {
    //         if (scrollChild.dataset.isUsedInScroll) {
    //             return
    //         } else {
    //             scrollChild.dataset.isUsedInScroll = 'true';
    //         }
    //     }
    //
    //     if (selectedChatId && makeRequest) {
    //         isLoading = true;
    //         testFetchCount++;
    //         await fetchMessageHistoryAndShow(selectedChatId, false, scrollDirection, lmCreatedDate, scrollDirection === 'UP' ? 'afterbegin' : 'beforeend');
    //         isLoading = false;
    //     }
    //
    // });
    // // unread message observer
    // messageUnreadObserver = new IntersectionObserver((entries, observer) => {
    //     // console.log("messageUnreadObserver: " + entries);
    //     entries.forEach(entry => {
    //         if (entry.isIntersecting) {
    //             // console.log("inside IntersectionObserver");
    //             // get element
    //             const messageEl = entry.target;
    //             if (messageEl.dataset.senderId === currentProfileId) {
    //                 return;
    //             }
    //             // 1. Mark as read visually (remove unread class)
    //             messageEl.classList.remove('unread');
    //             // 2. Send to backend (or handle as needed)
    //             // console.log(messageEl.dataset.messageId);
    //             markMessageAsRead(messageEl.dataset.messageId);
    //             // 3. Stop watching this message
    //             observer.unobserve(messageEl);
    //             // decrease unread message count
    //             decreaseUnreadMessageCount(selectedChatId);
    //         }
    //     });
    // }, {
    //     root: null, // defaults to viewport
    //     threshold: 0.5 // 50% of the message must be visible
    // });
    //
    // // Close tooltip when clicking outside
    // document.addEventListener('click', () => {
    //     const allToolTipClasses = document.querySelectorAll(".tooltip");
    //     allToolTipClasses.forEach(item => {
    //         item.classList.remove('show');
    //     })
    // });
    //
    // // Click outside modal to close
    // imagePreviewModalOverlay.addEventListener("click", function (event) {
    //     if (event.target === imagePreviewModalOverlay) {
    //         closeImagePreviewModal();
    //     }
    // });
    // // fetch chat (user and group) list
    // getChatList();

    async function sendMessage() {
        const messageContent = messageInput.value.trim();
        if (messageContent) {
            const chatMessage = {
                receiverId: selectedChatId,
                content: messageInput.value,
                chatType: selectedChatType,
                repliedMessageId: repliedMessageId,
                chatMessageType: 'TEXT'
            };
            if (selectedMessageId) {
                const response = await sendUpdateMessageRequest(selectedMessageId, chatMessage);
                chatMessage['id'] = selectedMessageId;
                updateMessage(chatMessage);
            } else {
                const response = await sendCreateMessageRequest(chatMessage);
                showMessage([response], "beforeend");
            }
            clearInput(); // clear input
            scrollToBottom();
        }
    }

    // *** socket methods ***
    async function connect() {
        const socket = new SockJS('http://localhost:8080/ws');
        stompClient = Stomp.over(socket);

        const jwt = localStorage.getItem("jwtToken");
        if (!jwt) {
            window.location = 'login.html';
        }

        connectedPromise = new Promise((resolve, reject) => {
            stompClient.connect({Authorization: 'Bearer ' + jwt}, () => {
                console.log("STOMP connected.");
                resolve(); // Connection ready
            }, (error) => {
                console.error("STOMP connection error:", error);
                reject(error);
            });
        });

        return connectedPromise;
        // await stompClient.connect({
        //     Authorization: 'Bearer ' + jwt // custom header
        // }, function (frame) {
        //     console.log('Connected: ' + frame);
        // }, (error) => console.error("Connection error", error));
    }

    function disconnect() { //TODO disconnectda ish bor
        if (stompClient !== null) {
            // Send leave notification
            const chatMessage = {
                sender: nameInput.value,
                content: nameInput.value + ' left!',
                type: 'LEAVE'
            };
            // stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));

            stompClient.disconnect();
            console.log("Disconnected");

            // Disable UI elements
            disconnectBtn.disabled = true;
            sendBtn.disabled = true;
            nameInput.disabled = false;
        }
    }

    function openChat() {
        chatItems.forEach(item => {
            item.addEventListener('click', () => {
                // Activate chat area
                chatArea.classList.add('active');

                // Optionally update title or load messages
                const name = item.getAttribute('data-name');
                chatTitle.textContent = name;
            });
        });
    }

});

