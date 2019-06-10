$("body").css("background-color", "#303030");
$("body").css("background-image", 'url(' +"https://www.safwallpapers.com/files/1521133753_255a814cd2bfedb94e81b2a6f783a2d2.jpg"+ ')');
$("body").css("background-size", 'cover');
$("body").css("background-repeat", 'no-repeat');

$(document).ready(() =>
{
    const $menuButton = $('.menu-button');
    const $navDropdown = $('#nav-dropdown');

    const $loginButton = $(".login-button");
    const $loginForm = $(".login-form");
    const $signInButton = $('.sign-in-button');

    const $regButton = $('.register-button');
    const $regForm = $(".register-form");
    const $registerUserButton = $('.reg-button');

    const $browseMovie = $('.browse-movie');
    const $browseForm = $('.movie-form');
    const $browseButton = $('.browse-button');

    const $adv = $('.advanced');
    const $advForm = $(".advanced-form");
    const $advButton = $(".adv-button");


    const $cart = $('.cart');
    const $check = $('.checkout');
    const $shoppingCart = $('.shopping-cart');

    const $history = $('.history');
    const $base = "http://andromeda-70.ics.uci.edu:5399/api/g";
    //Default State, not logged in presets
    $shoppingCart.hide();
    $history.hide();
    // $adv.hide();
    //

    $registerUserButton.click(function(event)
    {
        event.preventDefault();

        let username = $(".email").val();// Extract data from search input box to be the title argument
        let password = $(".password-field").val();
        var newPass = password.split('');
        var request = {"email" : username, "password" : newPass};
        $.ajax({
            "headers" : {
                "Accept": "application/json",
                "ContentType": "application/json"
            },
            type: "POST",
            crossDomain: true,
            url: $base+"/idm/register",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(request),
            success: function(dataG, statusG,xhrG)
            {
                if (statusG === "nocontent")
                {
                    setTimeout(getReport.bind(this, null, null, xhrG.getResponseHeader("transactionID")), 500);
                }
            },
            error: function (jqXhr, textStatus, errorMessage)
            {
                alert('Error: ' + errorMessage);
            }
        });
    });

    $signInButton.click(function(event)
    {
        event.preventDefault();
        let username = $(".username").val() // Extract data from search input box to be the title argument
        let password = $(".password").val() // Extract data from search input box to be the title argument
        var newPass = password.split('')
        var request = {"email" : username, "password" : newPass};
        $.ajax({
            "headers" : {
                "Accept": "application/json",
                "ContentType": "application/json"
            },
            type: "POST",
            crossDomain: true,
            url: $base+"/idm/login",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(request),
            success: function(dataG, statusG,xhrG)
            {
                if (statusG === "nocontent")
                {
                    setTimeout(getReport.bind(this, username, null, xhrG.getResponseHeader("transactionID")), 500);
                }
            },
            error: function (jqXhr, textStatus, errorMessage)
            { // error callback
                alert('Error: ' + errorMessage);
            }
        });
    });

    $check.click(function(event)
    {
        let movieDom2 = $('.movie');
        movieDom2.empty();
        let movieDom1 = $('.movies');
        movieDom1.empty();
        let movieDom3 = $('.cart-items');
        movieDom3.empty();
        event.preventDefault();
        // SEND AJAX REQUESTS TO INSERT BOGUS INFO INTO CC AND CC CREDIT CARD THEN SEND REQUEST TO CHECK OUT
        var CCrequest = {"id" : "9999888877776666123", "firstName": "my First Name", "lastName": "my Last Name", "expiration": "2022-02-02" };
        console.log("Stringified CCrequest: "+JSON.stringify(CCrequest));
        $.ajax({
            "headers" : {
                "Accept": "application/json",
                "ContentType": "application/json",
                "email": getCookie("email"),
                "sessionID" : getCookie("sessionID")
            },
            type: "POST",
            crossDomain: true,
            url: $base+"/billing/creditcard/insert",
            xhrFields: {
                withCredentials: true
            },
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(CCrequest),
            success: function(dataG, statusG,xhrG)
            {
                if (statusG === "nocontent")
                {
                    setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                }
            },
            error: function (jqXhr, textStatus, errorMessage)
            { // error callback
                alert('Error: ' + errorMessage);
            }
        });
    });
    $cart.click(function(event)
    {
        let movieDom2 = $('.movie');
        movieDom2.empty();
        let movieDom1 = $('.movies');
        movieDom1.empty();
        // let movieDom3 = $('.cart-items');
        // movieDom3.empty();
        event.preventDefault();
        var request = {"email" : getCookie("email")};
        // console.log("Stringified request: "+JSON.stringify(request));
        $.ajax({
            "headers" : {
                "Accept": "application/json",
                "ContentType": "application/json",
                "email": getCookie("email"),
                "sessionID" : getCookie("sessionID")
            },
            type: "POST",
            crossDomain: true,
            url: $base+"/billing/cart/retrieve",
            xhrFields: {
                withCredentials: true
            },
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(request),
            success: function(dataG, statusG,xhrG)
            {
                if (statusG === "nocontent")
                {
                    setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                }
            },
            error: function (jqXhr, textStatus, errorMessage)
            { // error callback
                alert('Error: ' + errorMessage);
            }
        });
    });
    $history.click(function(event)
    {
        let movieDom3 = $('.cart-items');
        movieDom3.empty();
        console.log("clicked on Order History");
        event.preventDefault();
        var request = {"email" : getCookie("email")};
        $.ajax({
            "headers" : {
                "Accept": "application/json",
                "ContentType": "application/json",
                "email": getCookie("email"),
                "sessionID" : getCookie("sessionID")
            },
            type: "POST",
            crossDomain: true,
            url: $base+"/billing/order/retrieve",
            xhrFields: {
                withCredentials: true
            },
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(request),
            success: function(dataG, statusG,xhrG)
            {
                if (statusG === "nocontent")
                {
                    setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                }
            },
            error: function (jqXhr, textStatus, errorMessage)
            { // error callback
                alert('Error: ' + errorMessage);
            }
        });
    });
    $browseMovie.on('click', () =>
    {
        $browseForm.show();
    });
    $browseButton.click(function(event)
    {
        event.preventDefault();
        let movieDom2 = $('.movie');
        movieDom2.empty();
        let movieDom1 = $('.movies');
        movieDom1.empty();
        let movieDom3 = $('.cart-items');
        movieDom3.empty();

        genre = $(".browse-genre").val() // Extract data from search input box to be the title argument
        limit = $(".limit").val()
        offset = $(".offset").val()

        var x = getCookie("email");
        var y = getCookie("sessionID");
        // console.log(x);
        // console.log(y);
        if (x.length == 0 || y.length == 0 ){ //VERIFY THIS WORKS WITHOUT LOGGING IN
            x = "quickSearch@uci.edu";
            y = "1234test";
        }
        $.ajax({
            "headers" : {
                "Accept": "application/json",
                "ContentType": "application/json",
                "email": x,
                "sessionID" : y
            },
            type: "GET",
            crossDomain: true,
            url: $base+"/movies/search?limit="+limit+"&offset="+offset+"&genre="+genre,
            xhrFields: {
                withCredentials: true
            },
            dataType: "json",
            contentType: "application/json",
            success: function(dataG, statusG, xhrG)
            {
                $browseForm.fadeOut('400');
                if (statusG === "nocontent")
                {
                    $browseForm.fadeOut('400');
                    setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                    bool = 0; // false means that were inside browse
                    // setTimeout(getReport.bind(this, x, y, xhrG.getResponseHeader("transactionID")), 500);
                }
                else {
                    $browseForm.hide();
                    alert("Session not verified, please try logging in");
                }
            },
            error: function (jqXhr, textStatus, errorMessage)
            { // error callback
                $browseForm.fadeOut('400');
                alert('Error: ' + errorMessage);
            }
        });
    });


    $adv.on('click', () =>
    {
        $advForm.show();
    });
    $advButton.click(function(event)
    {
        event.preventDefault();
        title = $(".adv-title").val() // Extract data from search input box to be the title argument
        genre = $(".adv-genre").val() // Extract data from search input box to be the title argument
        limit = $(".adv-limit").val()
        offset = $(".adv-offset").val();
        vear = $(".year").val()
        var director = $('.director').val()
        var x = getCookie("email");
        var y = getCookie("sessionID");
        // console.log(x);
        // console.log(y);
        if (x.length == 0 || y.length == 0 ){ //VERIFY THIS WORKS WITHOUT LOGGING IN
            x = "quickSearch@uci.edu";
            y = "1234test";
        }
        $.ajax({
            "headers" : {
                "Accept": "application/json",
                "ContentType": "application/json",
                "email": x,
                "sessionID" : y
            },
            type: "GET",
            crossDomain: true,
            url: $base+"/movies/search?limit="+limit+"&offset="+offset+"&title="+title+"&genre="+genre+"&year="+year+"&director="+director,
            xhrFields: {
                withCredentials: true
            },
            dataType: "json",
            contentType: "application/json",
            success: function(dataG, statusG, xhrG)
            {
                $advForm.fadeOut('400');
                if (statusG === "nocontent")
                {
                    setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                    bool = 1; // this is true means that inside advance search
                }
                else {
                    alert("Session not verified, please try logging in");
                }
            },
            error: function (jqXhr, textStatus, errorMessage)
            { // error callback
                $advForm.fadeOut('400');
                alert('Error: ' + errorMessage);
            }
        });
    });

    $menuButton.on('click', () =>
    {
        $navDropdown.show();
        // $menuButton.css({color: '#ffffff', backgroundColor: '#ff4d4d'});
        // $menuButton.animate({fontSize: '24px'}, 200);
    });

    $navDropdown.on('mouseleave', () =>
    {
        $navDropdown.fadeOut('400');
        // $menuButton.css({color: '#000000', backgroundColor: '#ff3333'});
        // $menuButton.animate({fontSize:'18px'},200);
    });
    $browseForm.on('mouseleave', () =>
    {
        $navDropdown.fadeOut('400');
        // $menuButton.css({color: '#000000', backgroundColor: '#ff3333'});
        // $menuButton.animate({fontSize:'18px'},200);
    });

    $loginButton.on("click", () =>
    {
        $loginForm.show();
        // $loginButton.css({color: '#ffffff', backgroundColor: '#ff4d4d'});
        // $loginButton.animate({fontSize: '24px'}, 200);
    });

    $loginForm.on('mouseleave', () =>
    {
        $loginForm.fadeOut('400');
        // $loginButton.css({color: '#000000', backgroundColor: '#ff3333'});
        // $loginButton.animate({fontSize:'18px'},200);
    });

    $regButton.on("click", () =>
    {
        $regForm.show();
        // $regButton.css({color: '#ffffff', backgroundColor: '#ff4d4d'});
        // $regButton.animate({fontSize: '24px'}, 200);
    });

    $regForm.on('mouseleave', () =>
    {
        $regForm.fadeOut('400');
        // $regButton.css({color: '#000000', backgroundColor: '#ff3333'});
        // $regButton.animate({fontSize:'18px'},200);
    });
    var response1 = "";
    var resultC = "";
    var bool = 0;//default to false
    var session = "";
    var movies = "";
    var SingleMovie = "";
    var items = "";
    var redirectURL = "";
    var transactions = "";

    var title = ""; // Extract data from search input box to be the title argument
    var genre =  ""; // Extract data from search input box to be the title argument
    var limit =  "";
    var offset =  "";
    var year = "";
    var director =  "";

    var newOffset = 0;

    function getReport(email, sessionID, transId)
    {
        console.log("Inside report func");
        $.ajax({
            "headers" : {
                "Accept": "application/json",
                "ContentType": "application/json",
                "email": email,
                "sessionID" : sessionID,
                "transactionID" : transId
            },
            type: "GET",
            crossDomain: true,
            url: $base+"/report",
            dataType: "json",
            // async: false,
            contentType: "application/json",
            success:function (response) {
                if (Boolean(bool) == false){ //bool = 0, then means inside browse
                    $browseForm.hide();
                }
                else if (Boolean(bool) == true) { // bool = 1, inside adv
                    $advForm.hide();
                }
                if (response === 'undefined')
                {
                    // response = null;
                    setTimeout(getReport.bind(this, email, sessionID, transId), 500);
                }
                else if (response !== 'undefined')
                {
                    // console.log(response);
                    if (typeof response === 'undefined')
                    {
                        console.log(response);
                        setTimeout(getReport.bind(this, email, sessionID, transId), 500);
                    }
                    else if (response["resultCode"] == 325 || response["resultCode"] == 3200)
                    {
                        console.log(response["message"]);
                        var Crequest = {"email": getCookie("email"), "firstName": "my First Name", "lastName": "my Last Name", "ccId": "9999888877776666123", "address": "1234, Campus Dr., Irvine, CA, 92697"};
                        // console.log("Stringified CCrequest: "+JSON.stringify(Crequest));
                        $.ajax({
                            "headers" : {
                                "Accept": "application/json",
                                "ContentType": "application/json",
                                "email": getCookie("email"),
                                "sessionID" : getCookie("sessionID")
                            },
                            type: "POST",
                            crossDomain: true,
                            url: $base+"/billing/customer/insert",
                            xhrFields: {
                                withCredentials: true
                            },
                            dataType: "json",
                            contentType: "application/json",
                            data: JSON.stringify(Crequest),
                            success: function(dataG, statusG,xhrG)
                            {
                                if (statusG === "nocontent")
                                {
                                    setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                                }
                            },
                            error: function (jqXhr, textStatus, errorMessage)
                            { // error callback
                                alert('Error: ' + errorMessage);
                            }
                        });
                    }
                    else if (response["resultCode"] == 333 || response["resultCode"] == 3300)
                    {
                        var request = {"email" : getCookie("email")};
                        $.ajax({
                            "headers" : {
                                "Accept": "application/json",
                                "ContentType": "application/json",
                                "email": getCookie("email"),
                                "sessionID" : getCookie("sessionID")
                            },
                            type: "POST",
                            crossDomain: true,
                            url: $base+"/billing/order/place",
                            xhrFields: {
                                withCredentials: true
                            },
                            dataType: "json",
                            contentType: "application/json",
                            data: JSON.stringify(request),
                            success: function(dataG, statusG,xhrG)
                            {
                                if (statusG === "nocontent")
                                {
                                    setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                                }
                            },
                            error: function (jqXhr, textStatus, errorMessage)
                            { // error callback
                                alert('Error: ' + errorMessage);
                            }
                        });
                    }
                }
                    response1 = response["message"];
                    resultC = response["resultCode"];

                    if (resultC != 333 || resultC != 3300 || resultC != 3200 || resultC != 325 || resultC != 311)
                    {
                        alert('Alert: ' + response1);

                    }
                    session = response["sessionID"];
                    movies = response["movies"];
                    SingleMovie = response["movie"];
                    items = response["items"];
                    redirectURL = response["redirectURL"];
                    transactions = response["transactions"];
                    if (resultC == 110)
                    {
                        $regButton.hide();
                        $regForm.hide();
                    }
                    else if (resultC == 120)
                    {
                        // console.log("About to create the cookies");
                        setCookie("email", email, 365);
                        setCookie2("sessionID", session, 365);
                        $loginButton.hide();
                        $loginForm.hide();
                        $shoppingCart.show();
                        $history.show();
                        $adv.show();
                        // $cart.show();
                        // $check.show();
                    }
                    else if (resultC == 250 ){
                        console.log("Trying to go into Detail Page");
                        if (SingleMovie != null){
                            handleResult2(SingleMovie);
                        }
                    }
                    else if (resultC == 210 || resultC == 250)
                    {
                        // console.log("Got movies");
                        // movies = response["movies"];
                        if (Boolean(bool) == false){ //bool = 0, then means inside browse
                            $browseForm.hide();
                        }
                        else if (Boolean(bool) == true) { // bool = 1, inside adv
                            $advForm.hide();
                        }
                        if (movies != null){
                            console.log("Movie List Page");
                            handleResult(movies);
                        }
                        if (SingleMovie != null){
                            console.log("Movie Detail Page");
                            handleResult2(SingleMovie);
                        }
                    }
                    else if (resultC == 3130 || resultC == 312 ){ // cart was retrieved successfully, or no items were found
                        handleResult3(items);
                    }
                    else if (resultC == 3400)
                    {
                        window.open(redirectURL, "", "width=700,height=600");

                    }
                    else if (resultC == 3410){
                        console.log(transactions);
                        handleResult4(transactions);
                    }
                    else if (resultC == 3120 || resultC == 3110 || resultC == 3140 || resultC == 311) { // cart item was deleted,inserted, or cleared, or dup insertion so fire request to retrieve the new cart
                        var request = {"email" : getCookie("email")};
                        let movieDom2 = $('.movie');
                        movieDom2.empty();
                        $.ajax({
                            "headers" : {
                                "Accept": "application/json",
                                "ContentType": "application/json",
                                "email": getCookie("email"),
                                "sessionID" : getCookie("sessionID")
                            },
                            type: "POST",
                            crossDomain: true,
                            url: $base+"/billing/cart/retrieve",
                            xhrFields: {
                                withCredentials: true
                            },
                            dataType: "json",
                            contentType: "application/json",
                            data: JSON.stringify(request),
                            success: function(dataG, statusG,xhrG)
                            {
                                if (statusG === "nocontent")
                                {
                                    setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                                }
                            },
                            error: function (jqXhr, textStatus, errorMessage)
                            { // error callback
                                alert('Error: ' + errorMessage);
                            }
                        });
                    }
            },
            error: function (jqXhr, textStatus, errorMessage)
            { // error callback
                alert('Error: ' + errorMessage);
            }
        });
    }
    function handleResult(movies) {
        console.log(movies);
        let movieDom = $('.movies');
        movieDom.empty();
        let rowHTML = "<table border=\"1\"><tr><td>Movie ID</td><td>Title</td><td>Director</td><td>Year</td><td>Rating</td><td>Votes</td></tr>";
        let movieList = movies;

        for (let i = 0; i < movieList.length; ++i) {
            rowHTML += "<tr>";
            let movieObject = movieList[i];
            let movieId = movieObject["movieId"];
            rowHTML += "<td>" + "<a href=\"" + movieId + "\">" + movieObject["movieId"] + "</a>" + "</td>"; //+ "<a href=\"" + movieId + "\">"
            rowHTML += "<td>" + movieObject["title"] + "</td>";
            rowHTML += "<td>" + movieObject["director"] + "</td>";
            rowHTML += "<td>" + movieObject["year"] + "</td>";
            rowHTML += "<td>" + movieObject["rating"] + "</td>";
            rowHTML += "<td>" + movieObject["numVotes"] + "</td>";
            rowHTML +="<td style='border-color:transparent' >" + "<input type=\'submit\' class=\"btn-cart\" value=\'Add to Cart\'/>" + "<input type = 'number'  min=\"1\" max=\"100\" step=\"1\" class = \"quantity\" style=\"position: relative\"/>" + "</td>";
            rowHTML += "</tr>";
        }
        rowHTML += "<tfoot>";
        rowHTML += "<tr>";
        rowHTML +="<td style='border-color:transparent' >" + "<button type=\'button\' class=\"btn-previous\"> Previous</button>" + "</td>";
        rowHTML +="<td style='border-color:transparent' >" + "</td>";
        rowHTML +="<td style='border-color:transparent' >" + "</td>";
        rowHTML +="<td style='border-color:transparent' >" + "</td>";
        rowHTML +="<td style='border-color:transparent' >" + "</td>";
        rowHTML +="<td style='border-color:transparent' >" + "<button type=\'button\' class=\"btn-next\"> Next</button>" + "</td>";
        rowHTML += "</tr>";
        rowHTML += "</tfoot>";
        rowHTML += "</table>";
        movieDom.append(rowHTML);

        $(".btn-previous").click(function(event) {
            if (newOffset === 0)
            {
                console.log("parsedint: " + parseInt(offset));
                newOffset = parseInt(offset);
                console.log("Newoffset: " + newOffset);

            }
            event.preventDefault();
            movieDom.empty();
            newOffset = newOffset-1;
            console.log("After Decrementing: " + newOffset);
            $.ajax({
                "headers" : {
                    "Accept": "application/json",
                    "ContentType": "application/json",
                    "email": getCookie("email"),
                    "sessionID" : getCookie("sessionID")
                },
                type: "GET",
                crossDomain: true,
                url: $base+"/movies/search?limit="+limit+"&offset="+newOffset+"&title="+title+"&genre="+genre+"&year="+year+"&director="+director,
                xhrFields: {
                    withCredentials: true
                },
                dataType: "json",
                contentType: "application/json",
                success: function(dataG, statusG, xhrG)
                {
                    $advForm.fadeOut('400');
                    if (statusG === "nocontent")
                    {
                        setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                        bool = 1; // this is true means that inside advance search
                    }
                    else {
                        alert("Session not verified, please try logging in");
                    }
                },
                error: function (jqXhr, textStatus, errorMessage)
                { // error callback
                    $advForm.fadeOut('400');
                    alert('Error: ' + errorMessage);
                }
            });
        });
        $(".btn-next").click(function(event) {
            if (newOffset === 0){
                console.log("parsedint: " + parseInt(offset));
                newOffset = parseInt(offset);
                console.log("Newoffset: " + newOffset);
            }
            newOffset = newOffset+1;
            event.preventDefault();
            movieDom.empty();
            $.ajax({
                "headers" : {
                    "Accept": "application/json",
                    "ContentType": "application/json",
                    "email": getCookie("email"),
                    "sessionID" : getCookie("sessionID")
                },
                type: "GET",
                crossDomain: true,
                url: $base+"/movies/search?limit="+limit+"&offset="+newOffset+"&title="+title+"&genre="+genre+"&year="+year+"&director="+director,
                xhrFields: {
                    withCredentials: true
                },
                dataType: "json",
                contentType: "application/json",
                success: function(dataG, statusG, xhrG)
                {
                    $advForm.fadeOut('400');
                    if (statusG === "nocontent")
                    {
                        setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                        bool = 1; // this is true means that inside advance search
                    }
                    else {
                        alert("Session not verified, please try logging in");
                    }
                },
                error: function (jqXhr, textStatus, errorMessage)
                { // error callback
                    $advForm.fadeOut('400');
                    alert('Error: ' + errorMessage);
                }
            });
        });
        $(".btn-cart").click(function(event) {
            // movieDom2.empty(); // Clear the previous results
            event.preventDefault();
            var $row = $(this).closest("tr");    // Find the row
            let quantity = $row.find(".quantity").val();
            console.log("Quantity: "+ quantity);
            var request = {"email" : getCookie("email"), "movieId" : movieId, "quantity" : quantity};
            console.log("Stringified request to delete: "+JSON.stringify(request));
            $.ajax({
                "headers" : {
                    "Accept": "application/json",
                    "ContentType": "application/json",
                    "email": getCookie("email"),
                    "sessionID" : getCookie("sessionID")
                },
                type: "POST",
                crossDomain: true,
                url: $base+"/billing/cart/insert",
                dataType: "json",
                data: JSON.stringify(request),
                contentType: "application/json",
                success: function(dataG, statusG, xhrG)
                {
                    $browseForm.hide();
                    if (statusG === "nocontent")
                    {
                        setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                    }
                },
                error: function (jqXhr, textStatus, errorMessage)
                { // error callback
                    $browseForm.hide();
                    alert('Error: ' + errorMessage);
                }
            });
        });
        $("tr").click(function(event) {
            event.preventDefault();
            movieDom.empty();
            let href  = $(this).find("a").attr("href");
            // console.log(href);
            $.ajax({
                "headers" : {
                    "Accept": "application/json",
                    "ContentType": "application/json",
                    "email": getCookie("email"),
                    "sessionID" : getCookie("sessionID")
                },
                type: "GET",
                crossDomain: true,
                url: $base+"/movies/get/" + href,
                dataType: "json",
                contentType: "application/json",
                success: function(dataG, statusG, xhrG)
                {
                    if (statusG === "nocontent")
                    {
                        setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                    }
                },
                error: function (jqXhr, textStatus, errorMessage)
                { // error callback
                    alert('Error: ' + errorMessage);
                }
            });
        });
    }
    function handleResult2(movie) {
        // console.log(movie);
        let movieDom2 = $('.movie');
        movieDom2.empty(); // Clear the previous results

        // Manually build the HTML Table with the response
        let rowHTML = "<table border=\"1\" width=\"100%\"><tr><td>Movie ID</td><td>Title</td><td>Director</td><td>Year</td><td>Backdrop Path</td><td>Budget</td><td>Overview</td><td>Poster Path</td><td>Revenue</td><td>Rating</td><td>Votes</td><td>Genres</td><td>Stars</td></tr>";
        let movieList = movie;

        var movieId = "";
        for (let i = 0; i < movieList.length; ++i) {
            rowHTML += "<tr>";
            let movieObject = movieList[i];
            movieId = movieObject["movieId"];
            // rowHTML += "<td>" + "<a href=\"" + movieId + "\">" + movieObject["movieId"] + "</a>" + "</td>";
            rowHTML += "<td>" + movieObject["movieId"] + "</td>";
            rowHTML += "<td>" + movieObject["title"] + "</td>";
            rowHTML += "<td>" + movieObject["director"] + "</td>";
            rowHTML += "<td>" + movieObject["year"] + "</td>";
            if (typeof movieObject["backdrop_path"] != 'undefined' ){
                rowHTML += "<td style=\"width: 50%; white-space: pre;\">" + '<img src=\"http://image.tmdb.org/t/p/w92/' + movieObject["backdrop_path"] + '\"' + "alt=\'bogus\' /></td>";
            }
            else {
                rowHTML += "<td style=\"width: 50%; white-space: pre;\">" + movieObject["backdrop_path"] + "</td>";
            }
            rowHTML += "<td >" + movieObject["budget"] + "</td>";
            rowHTML += "<td style=\"width: 50%; white-space: normal;\">" + movieObject["overview"] + "</td>";
            if (typeof movieObject["poster_path"] !== 'undefined') {
                rowHTML += "<td style=\"width: 50%; white-space: pre;\">" + '<img src=\"http://image.tmdb.org/t/p/w92/' + movieObject["poster_path"] + '\"' + "alt=\'bogus2\' /></td>";
            }
            else {
                rowHTML += "<td style=\"width: 50%; white-space: pre;\">" + movieObject["poster_path"] + "</td>";
            }
            rowHTML += "<td>" + movieObject["revenue"] + "</td>";
            rowHTML += "<td>" + movieObject["rating"] + "</td>";
            rowHTML += "<td>" + movieObject["numVotes"] + "</td>";
            var genreList = movieObject["genres"];
            var starsList =  movieObject["stars"];
        }
        let genresString = "";
        for (let x = 0; x < genreList.length; x++){
            let genreObject = genreList[x];
            genresString += (genreObject["name"]) + ", ";
            // console.log("Genre string: " + genreObject["name"]);
        }
        let starString = "";

        for (let y = 0; y < starsList.length; y++){
            let starObject = starsList[y];
            // console.log("Star string: " + starObject["name"]);

            starString += starObject["name"] + ", ";
        }
        rowHTML += "<td>" + genresString.replace(/,\s*$/, "") + "</td>";
        rowHTML += "<td>" + starString.replace(/,\s*$/, "") + "</td>";

        rowHTML += "</tr>";
        rowHTML += "<tfoot>";
        rowHTML += "<tr>";
        rowHTML +="<td style='border-color:transparent' >" + "<input type=\'submit\' class=\"btn-rate\" value=\'Rate Movie\' />" + "<input type = 'number'  min=\"1\" max=\"10\" step=\"0.1\" class = \"rating\" style=\"position: relative\"/>" + "</td>";
        rowHTML +="<td style='border-color:transparent' >" + "<input type=\'submit\' class=\"btn-cart\" value=\'Add to Cart\'/>" + "<input type = 'number'  min=\"1\" max=\"100\" step=\"1\" class = \"quantity\" style=\"position: relative\"/>" + "</td>";
        // rowHTML += "<button class=\"btn-cart\">Add to Cart</button>";
        rowHTML += "</tr>";
        rowHTML += "</tfoot>";

        rowHTML += "</table>";

        movieDom2.append(rowHTML);
        $(".btn-rate").click(function(event) {
            // movieDom2.empty(); // Clear the previous results
            // ONLY EMPTY THE MOVIEDOM RESULTS IF THE CODE IS 250 IN REPORT
            event.preventDefault();
            var $row = $(this).closest("tr");    // Find the row
            let rating = $row.find(".rating").val();
            console.log("Rating: "+ rating);
            var request = {"id" : movieId, "rating" : rating};
            console.log("Stringified request to delete: "+JSON.stringify(request));
            $.ajax({
                "headers" : {
                    "Accept": "application/json",
                    "ContentType": "application/json",
                    "email": getCookie("email"),
                    "sessionID" : getCookie("sessionID")
                },
                type: "POST",
                crossDomain: true,
                url: $base+"/movies/rating",
                dataType: "json",
                data: JSON.stringify(request),
                contentType: "application/json",
                success: function(dataG, statusG, xhrG)
                {
                    $browseForm.hide();
                    if (statusG === "nocontent")
                    {
                        setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                    }
                },
                error: function (jqXhr, textStatus, errorMessage)
                { // error callback
                    $browseForm.hide();
                    alert('Error: ' + errorMessage);
                }
            });
        });
        // ADD FUNCTIONALITY HERE FOR RATING A MOVIE
        $(".btn-cart").click(function(event) {
            // movieDom2.empty(); // Clear the previous results
            event.preventDefault();
            var $row = $(this).closest("tr");    // Find the row
            let quantity = $row.find(".quantity").val();
            console.log("Quantity: "+ quantity);
            var request = {"email" : getCookie("email"), "movieId" : movieId, "quantity" : quantity};
            console.log("Stringified request to delete: "+JSON.stringify(request));
            $.ajax({
                "headers" : {
                    "Accept": "application/json",
                    "ContentType": "application/json",
                    "email": getCookie("email"),
                    "sessionID" : getCookie("sessionID")
                },
                type: "POST",
                crossDomain: true,
                url: $base+"/billing/cart/insert",
                dataType: "json",
                data: JSON.stringify(request),
                contentType: "application/json",
                success: function(dataG, statusG, xhrG)
                {
                    $browseForm.hide();
                    if (statusG === "nocontent")
                    {
                        setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                    }
                },
                error: function (jqXhr, textStatus, errorMessage)
                { // error callback
                    $browseForm.hide();
                    alert('Error: ' + errorMessage);
                }
            });
        });
    }
    function handleResult3(items) {
        let movieDom3 = $('.cart-items');
        movieDom3.empty(); // Clear the previous results
        let arr = new Array(0);
        if (items === undefined || items.length == 0) {
            items = arr;
        }
        // Manually build the HTML Table with the response
        let rowHTML = "<table border=\"1\" width=\"100%\"><tr><td>Movie ID</td><td>Quantity</td></tr>";
        let cartList = items;

        var movieId = "";
        for (let i = 0; i < cartList.length; ++i) {
            rowHTML += "<tr>";
            let cartObject = cartList[i];
            movieId = cartObject["movieId"];
            rowHTML += "<td>" + "<a href=\"" + movieId + "\">" + cartObject["movieId"] + "</a>" + "</td>";
            rowHTML += "<td>" + cartObject["quantity"] + "</td>";
            rowHTML +="<td style='border-color:transparent'>" + "<input type = 'number'  min=\"1\" max=\"100\" step=\"1\" class = \"quantity\" />" + "<input type=\'submit\' class=\"btn-update\" value=\'Update\' />" + "</td>";
            rowHTML +="<td style='border-color:transparent'>" + "<button class=\"btn-delete\">Remove</button>" + "</td>";
            rowHTML += "</tr>";
        }
        rowHTML += "<tfoot>";
        rowHTML += "<tr>";
        rowHTML += "<td style='border-color:transparent'>";
        rowHTML += "<button class=\"btn-clear\">Clear Cart</button>";
        rowHTML += "</td>";
        rowHTML += "</tr>";
        rowHTML += "</tfoot>";
        rowHTML += "</table>";

        movieDom3.append(rowHTML);
        $(".btn-clear").click(function(event) {
            event.preventDefault();
            var request = {"email" : getCookie("email")};
            console.log("Stringified request to delete: "+JSON.stringify(request));
            $.ajax({
                "headers" : {
                    "Accept": "application/json",
                    "ContentType": "application/json",
                    "email": getCookie("email"),
                    "sessionID" : getCookie("sessionID")
                },
                type: "POST",
                crossDomain: true,
                url: $base+"/g/billing/cart/clear",
                dataType: "json",
                data: JSON.stringify(request),
                contentType: "application/json",
                success: function(dataG, statusG, xhrG)
                {
                    if (statusG === "nocontent")
                    {
                        setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                    }
                },
                error: function (jqXhr, textStatus, errorMessage)
                { // error callback
                    alert('Error: ' + errorMessage);
                }
            });
        });
        $(".btn-delete").click(function(event) {
            event.preventDefault();
            // movieDom3.empty();
            var $row = $(this).closest("tr");    // Find the row
            let href  = $row.find("a").attr("href");
            // console.log("Clicked on this movie to remove: " + href);
            var request = {"email" : getCookie("email"), "movieId" : href};
            // console.log("Stringified request to delete: "+JSON.stringify(request));
            $.ajax({
                "headers" : {
                    "Accept": "application/json",
                    "ContentType": "application/json",
                    "email": getCookie("email"),
                    "sessionID" : getCookie("sessionID")
                },
                type: "POST",
                crossDomain: true,
                url: $base+"/billing/cart/delete",
                dataType: "json",
                data: JSON.stringify(request),
                contentType: "application/json",
                success: function(dataG, statusG, xhrG)
                {
                    if (statusG === "nocontent")
                    {
                        setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                    }
                },
                error: function (jqXhr, textStatus, errorMessage)
                { // error callback
                    alert('Error: ' + errorMessage);
                }
            });
        });
        $(".btn-update").click(function(event) {
            event.preventDefault();
            // movieDom3.empty();
            var $row = $(this).closest("tr");    // Find the row
            let href  = $row.find("a").attr("href");
            let quantity = $row.find(".quantity").val();
            // console.log("Quantity: "+ quantity);
            var request = {"email" : getCookie("email"), "movieId" : href, "quantity" : quantity};
            // console.log("Stringified request to delete: "+JSON.stringify(request));
            $.ajax({
                "headers" : {
                    "Accept": "application/json",
                    "ContentType": "application/json",
                    "email": getCookie("email"),
                    "sessionID" : getCookie("sessionID")
                },
                type: "POST",
                crossDomain: true,
                url: $base+"/billing/cart/update",
                dataType: "json",
                data: JSON.stringify(request),
                contentType: "application/json",
                success: function(dataG, statusG, xhrG)
                {
                    if (statusG === "nocontent")
                    {
                        setTimeout(getReport.bind(this, getCookie("email"), getCookie("sessionID"), xhrG.getResponseHeader("transactionID")), 500);
                    }
                },
                error: function (jqXhr, textStatus, errorMessage)
                { // error callback
                    alert('Error: ' + errorMessage);
                }
            });
        });
        // ADD FUNCTIONALITY HERE FOR RATING A MOVIE

    }
    function handleResult4(transactions) {
        console.log(transactions);
        let transDom = $('.order-history');
        transDom.empty(); // Clear the previous results

        // Manually build the HTML Table with the response
        let rowHTML = "<table border=\"1\"><tr><td>Total</td><td>Fees</td><td>Purchase Date</td></tr>";
        let transList = transactions;
        let itemsArr = "";
        for (let i = 0; i < transList.length; ++i) {
            rowHTML += "<tr>";
            let transObject = transList[i];
            itemsArr = transObject["items"];
            rowHTML += "<td>" + "$" + transObject["amount"]["total"] + "</td>";
            rowHTML += "<td>" + "$" + transObject["transaction_fee"]["value"]+ "</td>";
            rowHTML += "<td>" + transObject["update_time"] + "</td>";
            rowHTML += "</tr>";
        }
        rowHTML += "</table>";
        rowHTML += "<table border=\"1\"><tr><td>MovieId</td><td>Quantity</td><td>Price</td><td>Discount</td><td>Date</td></tr>";
        for (let i = 0; i < itemsArr.length; ++i) {
            rowHTML += "<tr>";
            let itemsObj = itemsArr[i];
            rowHTML += "<td>" + itemsObj["movieId"] + "</td>";
            rowHTML += "<td>" + itemsObj["quantity"]+ "</td>";
            rowHTML += "<td>" + "$" + itemsObj["unit_price"] + "</td>";
            rowHTML += "<td>" + "%" + itemsObj["discount"] + "</td>";
            rowHTML += "<td>" + itemsObj["saleDate"]+ "</td>";
            rowHTML += "</tr>";
        }
        rowHTML += "</table>";
        transDom.append(rowHTML);
    }
    function setCookie(username, email, exdays)
    { //exdays is 365
        var d = new Date();
        d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
        var expires = "expires="+d.toUTCString();
        document.cookie = username + "=" + email + ";" + expires ; //+ ";path=/"
    }
    function setCookie2(sessionID, sess, exdays)
    { //exdays is 365
        var d = new Date();
        d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
        var expires = "expires="+d.toUTCString();
        document.cookie = sessionID + "=" + sess + ";" + expires ; //+ ";path=/"
    }
    function getCookie(cname)
    {
        var name = cname + "=";
        var ca = document.cookie.split(';');
        for(var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') {
                c = c.substring(1);
            }
            if (c.indexOf(name) == 0) {
                return c.substring(name.length, c.length);
            }
        }
        return "";
    }
});

