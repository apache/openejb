$(document).ready(function() {
  $('#aggregate').hide();
});

function checkBoxClicked(id, b) {
   $('.' + id).each(function(i, val) {
     if (b) {
       $(val).show('fast');
     } else {
       $(val).hide();
     }
     $(val).attr('checked', b);
   });
}

function selectCheckboxes(b) {
  $('input[type=checkbox]').each(function(i, val) {
      checkBoxClicked($(val).attr('id'), b);
      $(val).attr('checked', b);
  });
}

function showCheckboxes() {
    if ($('#checkboxes-check').is(':visible')) {
        $('#checkboxes-check').hide();
        $('#showCheckboxes').attr('value', 'Show API');
    } else {
        $('#checkboxes-check').show();
        $('#showCheckboxes').attr('value', 'Hide API');
    }
}

function aggregate(button) {
  if (button.value == 'Aggregate') {
    button.value = 'Split';
    $('#list').hide();
    $('#aggregate').show('fast');
  } else {
    button.value = 'Aggregate';
    $('#list').show('fast');
    $('#aggregate').hide();
  }
  // order to show selected whic are in non slected too
  $('input[type=checkbox,checked=false]').each(function(i, val) {
      checkBoxClicked($(val).attr('id'), false);
  });
  $('input[type=checkbox,checked=true]').each(function(i, val) {
      checkBoxClicked($(val).attr('id'), true);
  });
}
