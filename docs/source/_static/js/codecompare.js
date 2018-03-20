$(document).ready(function() {
    $(".codecompare").each(function(index, el) {
        
        console.log(el);

        var block1 = $(el).children()[0];
        var block2 = $(el).children()[1];
        
        block1.setAttribute("class", "codecompare-block");
        block2.setAttribute("class", "codecompare-block");
    });
});