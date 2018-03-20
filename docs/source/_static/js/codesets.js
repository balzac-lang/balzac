$(document).ready(function() {

    var mnemonic = {
        "codeset-oldsol": "0.3.1",
        "codeset-newsol": "0.4.2",
        "codeset-btcmongodb": "BTC MongoDB",
        "codeset-btcmysql": "BTC MySQL",
        "codeset-ethmongodb": "ETH MongoDB",
        "codeset-ethmysql": "ETH MySQL",
        "codeset-co2": "CO<sub>2</sub>",
        "codeset-java": "Java"
    };

    $(".codeset").each(function(index, el) {
        
        var codeset = $(el);
        var codesetChildren = codeset.children();
        var codeSnippetWidgets = $("<div>").addClass('codesnippet-widgets');

        /*
         * iteratively create the <span> buttons for each child
         */
        for (var i=0; i<codesetChildren.length; i++) {
            
            // read the child class starting with "codeset-"
            var child = codesetChildren[i];
            var codesetClassName = child.className.match(/codeset-[\w]*\b/)[0];

            // create a <span>
            var span = $("<span>");
            span.html(mnemonic[codesetClassName]);

            // define the behaviour on click
            span.click((function(i, codeset, codeSnippetWidgets) {

                return function () {

                    // change the current button class style
                    codeSnippetWidgets.children().each(function(index){
                        $(this).removeClass("current");
                    });
                    $(this).addClass("current")

                    // hide all the blocks except the selected one
                    codeset.children().each(function(index){
                        $(this).hide();
                    });
                    $(codesetChildren[i]).show();

                }
            })(i, codeset, codeSnippetWidgets));    // closure

            codeSnippetWidgets.append(span);        // append the button
        }

        codeSnippetWidgets.insertBefore(codeset);   // insert all the buttons

        $(codeSnippetWidgets.children()[0]).click();// click on the first one
    });
});