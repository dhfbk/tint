//(function($, window) {
//    var adjustAnchor = function() {
//
//        var $anchor = $(':target'),
//            fixedElementHeight = 100;
//
//        if ($anchor.length > 0) {
//
//            $('html, body')
//                .stop()
//                .animate({
//                    scrollTop: $anchor.offset().top - fixedElementHeight
//                }, 200);
//
//        }
//
//    };
//
//    $(window).on('hashchange load', function() {
//        adjustAnchor();
//    });
//
//})(jQuery, window);

// Support for smooth scrolling
// (simplified version, taken from http://stackoverflow.com/a/14805098/1173184)
$(window).load(function(){
    $('a[href^="#"]:not([href^="#carousel"]):not([data-toggle="dropdown"])').on('click', function(e) {

        // prevent default anchor click behavior
        e.preventDefault();

        // store hash
        var hash = this.hash;

        // animate
        $('html, body').animate({
            scrollTop: $(this.hash).offset().top - 100
        }, 300, function(){

            // when done, add hash to url
            // (default click behaviour)
            window.location.hash = hash;
        });

    });
});

// Proper tab selection (adding back attributes removed by maven/reflow skin)
$('.nav-tabs li a').attr('data-toggle', 'tab');
$('.nav-tabs li a').click(function (e) {
    e.preventDefault();
    e.stopImmediatePropagation();
    $(this).tab('show');
})
