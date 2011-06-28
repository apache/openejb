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
    $('div#examples').find('li' + filteringForExamples).show('slow');
    if (selectedClasses.length > 0) {
        $('div#examples').find('li:not(' + filteringForExamples + ').example').hide();
    }

    // filtering buttons (apis)
    if (correspondingExamples.length > 0) {
        var examples = new Array();
        $('div#examples').find('li' + filteringForExamples).each(function(i, val) {
            examples.push($(val).attr('example'));
        }); 

        $('div#checkboxes-check > ul > li > input[type=button].button').hide();
        for (var i = 0; i < examples.length; i++) {
            if (examples[i].length > 0) {
                $('li[example="' + examples[i].substring(examples[i].lastIndexOf('_') + 1, examples[i].length) + '"]').each(function(i, val) {
                    $buttons = $(val).attr('class');
                    $buttons = $buttons.substring('example '.length, $buttons.length);
                    $buttonsArray = $buttons.split(' ');
                    for (var b = 0; b < $buttonsArray.length; b++) {
                        if (shouldIHideIt(examples, $buttonsArray[b]) && selectedClasses.indexOf($buttonsArray[b]) == -1) {
                            $('input[api="' + $buttonsArray[b] + '"]').hide();
                        } else {
                            $('input[api="' + $buttonsArray[b] + '"]').show('slow');
                        }
                    }
                });
            }
        }alert('ok');
    } else {
        $('div#checkboxes-check > ul > li > input[type=button].button').show('slow');
    }
}

function shouldIHideIt(list, api) {
    for (var i = 0; i < list.length; i++) {
        if (list[i].length > 0) {
            $item = $('li[example="' + list[i].substring(list[i].lastIndexOf('_') + 1, list[i].length) + '"]').next().attr('class');
            if ($item != undefined && $item != false) {
                $apis = $item.split(' ');
                if ($apis.indexOf(api) == -1) {
                    return false;
                }
            }
        }
    }
    return true;
}
