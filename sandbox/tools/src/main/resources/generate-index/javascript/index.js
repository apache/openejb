var close = ' X';
var selectedClasses = new Array(); // classes to use

function filterExamples($button) {
    if ($button.selected) {
        $button.value = $button.value.substring(0, $button.value.length - close.length);
        for (var i = 0; i < selectedClasses.length; i++) {
            if (selectedClasses[i] == $($button).attr('api')) {
                selectedClasses.splice(i, 1);
                break;
            }
        }
    } else {
        $button.value = $button.value.concat(close);
        selectedClasses.push($($button).attr('api'));
    }
    $button.selected = !$button.selected;

    // refresh
    var filtering = '';
    if (selectedClasses.length > 0) {
        filtering = '.'.concat(selectedClasses.join("."));
    }

    $('div#examples').find('li').each(function(i, val) {
        $(val).show('slow');
    });
    $('div#examples').find('li:not(' + filtering + ')').each(function(i, val) {
        if ($(val).is('.example')) { // glossary shouldn't be filtered
            $(val).hide();
        }
    });
}
