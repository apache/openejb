#!/usr/local/bin/perl -w
#!/program_files/Perl/bin/perl -w
#
# Perl filter to SourceForge task email messages. 
# This script will reformat the email and resend it to another
# recipient, usually a group mailing list.
#
# Created by David Blevins <david.blevins@visi.com>
#
# $Id$
############################################################
#
# Constants
#
############################################################
$MAIL_CMD      = "| /usr/lib/sendmail -i -t";
$MAIL_TO       = 'openejb-development@lists.sourceforge.net';
$MAIL_FROM     = 'noreply@sourceforge.net';

############################################################
#
# Subroutines
#
############################################################

sub convertTaskHeader {
    local($taskId);
    local($subproject);
    local($summary);
    local($complete);
    local($status);
    local($assignedto);
    local($authority);
    local(@description);
    local(@email);

    #Read Header, Stop at "Description:"
    while (<STDIN>) {
        chomp;			# Drop the newline
        if (/^Task /) { s/Task ([^ ]*).*$/$1/; $taskId = $_; next;}
        if (/^Subproject:/) { s/.*: (.*)$/$1/; $subproject = $_; next;}
        if (/^Summary:/) { s/.*: (.*)/$1/; $summary = $_; next;}
        if (/^Complete:/) { s/.*: ([^ ]*)/$1/; $complete = $_; next;}
        if (/^Status:/) { s/.*: ([^ ]*)$/$1/; $status = $_; next;}
        if (/^Authority/) { s/.*: ([^ ]*)/$1/; $authority = $_; next;}
        if (/^Assigned to/) { s/.*: ([^ ]*)/$1/; $assignedto = $_; next;}
        if (/^Description:/) { s/.*: (.*)/$1/; push(@desription, "  ".$_); last;}
    }

    #Read Description, Stop at "Follow-Ups:"
    while (<STDIN>) {
        chomp;			# Drop the newline
        if (/^Follow-Ups:/) { last; }
        if (/^-+/){ next; }
        if (/^For more info, visit:/) { last; }
        else {
            push(@desription, "  ".$_);
        }
    }


    #Format header
    @header   = ("status","complete","assigned to","authority");
    @data     = ($status, $complete, $assignedto, $authority);
    $hFormat  = " %-10s/ %-10s/ %-15s/ %-15s";
    $dFormat  = " %-10s| %-10s| %-15s| %-15s";
    
    push(@email, $subproject." Task");
    push(@email, "");
    push(@email, "  ".$summary);
    push(@email, "");
    push(@email, sprintf( $hFormat, @header));
    push(@email, " ----------------------------------------------------------");
    push(@email, sprintf( $dFormat, @data));
    push(@email, "");
    foreach $line (@desription) {
        push(@email, $line);
    }

    if (/^Follow-Ups:/) {
        #Read Follow-Ups
        while (<STDIN>) {
            if (/^For more info, visit:/) { last; }
            if (/^Date:/){
                push(@email, &convertTaskFollowUps($_));
            }
        }
    }

    push(@email, "\n\n- - - - - - - - - -");
    while (<STDIN>) {
        if (/^http:/) {push(@email, $_);}
    }

        
    open(MAIL, $MAIL_CMD);
    print MAIL "From: $MAIL_FROM\n";
    print MAIL "To: $MAIL_TO\n";
    print MAIL "Reply-To: $MAIL_TO\n";
    print MAIL "Subject: [task $taskId] $summary\n\n";
    print(MAIL join("\n", @email));
    close(MAIL);

}

sub convertTaskFollowUps {
    local($person);
    local($dateTime);
    local(@comments);
    local(@text);

    chomp;
    s/.*: (.*)$/$1/; 
    $dateTime = $_;
    
    #Read Header, Stop at "Comment:"
    while (<STDIN>) {
        chomp;
        if (/^Comment:/) { last; }
        if (/^By:/) { s/.*: (.*)$/$1/; $person = $_; next;}
    }
    #Read Description, Stop at "Follow-Ups:"
    while (<STDIN>) {
        chomp;			# Drop the newline
        if (/^--------/) { last; }
        else {
            push(@comments, "  ".$_);
        }
    }
           
    push(@text,  "  ________________________________________________");
    $line = "  _${person} wrote, on ${dateTime}________________________________";
    push(@text, sprintf( "%.50s", $line));
    push(@text, "");
    
    foreach $line (@comments) {
        push(@text, $line);
    }

    @text;
}

&convertTaskHeader;

