//http://www.websocket.org/echo.html
var websocket;

function initWebsocket(websocketPath)
{
    var websocketURL = "ws://" + window.location.host + websocketPath;
    websocket = new WebSocket(websocketURL);
    websocket.onopen = function(evt) { onOpen(evt) };
    websocket.onclose = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { onMessage(evt) };
    websocket.onerror = function(evt) { onError(evt) };
}

function onOpen(evt)
{
    writeToScreen("CONNECTED");
}

function onClose(evt)
{
    writeToScreen("DISCONNECTED");
}

function onMessage(evt)
{
    writeToScreen(evt.data);
}

function onError(evt)
{
    writeToScreen("ERROR: " + evt.data);
}

function doSend(message)
{
    websocket.send(message);
}

function writeToScreen(message)
{
    $('#output').animate({scrollTop:$('#output').height()},'50');
    $('#output').append( "<br />" + message );
}


