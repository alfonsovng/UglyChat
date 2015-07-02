/*
Adapted from http://www.websocket.org/echo.html
*/
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
    writeToScreen("<div class='text-right'><span class='label success'>YOU ARE CONNECTED</span></div>");
}

function onClose(evt)
{
    writeToScreen("<div class='text-right'><span class='label warning'>YOU ARE DISCONNECTED</span></div>");
}

function onMessage(evt)
{
    var json = jQuery.parseJSON(evt.data);

    var type = json.type;

    var html;
    if(type == "message") {
        html = "<span style='background-color: " + json.color
            + "'>&nbsp;<span style='color: " + json.color + "; filter: invert(100%)'>"
            + json.user + "</span>&nbsp;</span> " + json.message + "<br />";
    } else if(type == "login") {
        html = "<div class='text-right'><span class='label info'>" + json.user  + " IS CONNECTED</span></div>"
    } else if(type == "logout") {
        html = "<div class='text-right'><span class='label secondary'>" + json.user  + " IS DISCONNECTED</span></div>"
    } else {
        html = "<div class='text-right'><span class='label alert'>ERROR: " + evt.data  + "</span></div>"
    }
    writeToScreen(html);
}

function onError(evt)
{
    writeToScreen("<div class='text-right'><span class='label alert'>ERROR: " + evt.data  + "</span></div>");
}

function doSend(message)
{
    var json = new Object();
    json.message = message;
    websocket.send(JSON.stringify(json));
}

function doLogout() {
    websocket.close();
}

function writeToScreen(html)
{
    $('#output').animate({scrollTop:$('#output').height()},'50');
    $('#output').append(html);
}
