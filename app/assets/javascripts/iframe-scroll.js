//fixes a weird behaviour where on the challenge/3ds checks within the iframe, the page 'looks' blank, but users need to instead scroll up.
document.getElementById('barclaycard-iframe').addEventListener('load', function (e) {
    window.scrollTo(0, 0);
});