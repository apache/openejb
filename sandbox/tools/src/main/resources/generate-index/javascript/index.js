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
        $('ul.api > li > input[type=button].button').each(function(i, val) {
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
                $(val).show('fast');
            } else {
                $(val).hide();
            }
        });

        // used only in click-filtering mode
        $('#api-info').hide();
    });
});

var close = ' X';
var selectedClasses = new Array(); // classes to use
var correspondingExamples = new Array(); // classes to use

function filterExamples($button) {
    // resetting filter by text input
    $('#searchbox').val('');

    // selecting the clicked button
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

    // refreshing
    var filteringForExamples = ''; // for examples
    if (selectedClasses.length > 0) {
        filteringForExamples = '.'.concat(selectedClasses.join('.'));
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
    $('ul#examples > li.example').show('fast');
    if (selectedClasses.length > 0) {
        $('ul#examples').find('li:not(' + filteringForExamples + ').example').hide();
    }

    // filtering buttons (apis)
    if (correspondingExamples.length > 0) {
        var examples = new Array();
        $('ul#examples').find('li' + filteringForExamples).each(function(i, val) {
            examples.push($(val).attr('example'));
        });
        examples = filterArray(examples);

        $('ul.api > li.api > input[type=button].button').hide();
        for (var i = 0; i < examples.length; i++) {
            $('li[example="' + examples[i].substring(examples[i].lastIndexOf('_') + 1, examples[i].length) + '"]').each(function(i, val) {
                $buttons = $(val).attr('class');
                $buttons = $buttons.substring('example '.length, $buttons.length);
                $buttonsArray = $buttons.split(' ');
                for (var b = 0; b < $buttonsArray.length; b++) {
                    if (shouldIHideIt(examples, $buttonsArray[b]) && selectedClasses.indexOf($buttonsArray[b]) == -1) {
                        $('input[api="' + $buttonsArray[b] + '"]').hide();
                    } else {
                        $('input[api="' + $buttonsArray[b] + '"]').show('fast');
                    }
                }
            });
        }

        $('#api-info').show();
        $('#api-info').text(examples.length + ' examples are matching');
    } else {
        $('ul.api > li.api > input[type=button].button').show('fast');
        $('#api-info').hide();
    }
}

function filterArray(list) {
    var out = new Array();
    for (var i = 0; i < list.length; i++) {
        if (list[i].length > 0) {
            out.push(list[i]);
        }
    }
    return out;
}

function shouldIHideIt(list, api) {
    for (var i = 0; i < list.length; i++) {
        $item = $('li[example="' + list[i].substring(list[i].lastIndexOf('_') + 1, list[i].length) + '"]').next().attr('class');
        if ($item != undefined && $item != false) {
            $apis = $item.split(' ');
            if ($apis.indexOf(api) == -1) {
                return false;
            }
        }
    }
    return true;
}
