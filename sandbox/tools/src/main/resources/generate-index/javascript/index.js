$(document).ready(function() {
    $('input[type=text]#searchbox').keyup(function() {
        input = $(this).val();
        if (input.length != 0) {
            var filter = input.split(' ');
            var regexps = new Array();
            var idx = 0;

            for (var i = 0; i < filter.length; i++) {
                if (!$.trim(filter[i]).length == 0) {
                    regexps[idx++] = new RegExp(filter[i],"i");
                }
            }
        }

        // filtering apis
        $('div#checkboxes-check > ul > li > input[type=button].button').each(function(i, val) {
                var toShow = false;
                if (input.length == 0) {
                    toShow = true;
                } else {
                    for (var i = 0; i < regexps.length; i++) {
                        if (regexps[i].test($(val).attr('value'))) {
                            toShow = true;
                            break;
                        }
                    }
                }
                if (toShow) {
                    $(val).show('slow');
                } else {
                    $(val).hide();
                }
            });
        });
});

var close = ' X';
var selectedClasses = new Array(); // classes to use
var correspondingExamples = new Array(); // classes to use

function filterExamples($button) {
    if ($button.selected) {
        $button.value = $button.value.substring(0, $button.value.length - close.length);
        for (var i = 0; i < selectedClasses.length; i++) {
            if (selectedClasses[i] == $($button).attr('api')) {
                selectedClasses.splice(i, 1);
                correspondingExamples.splice(i, 1);
                break;
            }
        }
    } else {
        $button.value = $button.value.concat(close);
        selectedClasses.push($($button).attr('api'));
        correspondingExamples.push($($button).attr('class'));
    }
    $button.selected = !$button.selected;

    // refresh
    var filteringForExamples = ''; // for examples
    if (selectedClasses.length > 0) {
        filteringForExamples = '.'.concat(selectedClasses.join("."));
    }

    var filteringForButtons = ''; // for buttons
    if (correspondingExamples.length > 0) {
        for (var i = 0; i < correspondingExamples.length; i++) {
            filteringForButtons = filteringForButtons.concat(' ').concat(correspondingExamples[i]);
        }
        var examplesForButtons = $.unique(filteringForButtons.split(' '));
        filteringForButtons = '.'.concat(examplesForButtons.join('.'));
    }

    // filtering examples
    $('div#examples').find('li' + filteringForExamples).each(function(i, val) {
        $(val).show('slow');
    });
    if (selectedClasses.length > 0) {
        $('div#examples').find('li:not(' + filteringForExamples + ').example').each(function(i, val) {
            $(val).hide();
        });
    }

    // filtering buttons (apis)
    $('div#checkboxes-check > ul > li > input[type=button].button' + filteringForButtons).each(function(i, val) {
        $(val).show('slow');
    });
    if (correspondingExamples.length > 0) {
        $('div#checkboxes-check > ul > li > input[type=button].button').not(filteringForButtons).each(function(i, val) {
            if (!$(val).attr('selected')) { // to be kept
                $(val).hide();
            }
        });
    }
}
