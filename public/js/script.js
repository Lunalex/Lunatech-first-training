$(function () {

    $('.delete-product-btn').click(function () {
        console.log("je suis dans le JS");
        var url =  this.getAttribute('data-url');
        var redirect =  this.getAttribute('data-redirect-url');
        var token = this.getAttribute('data-token');

        $.ajaxSetup({
            beforeSend: function(request) {
                request.setRequestHeader('Csrf-Token', token);
            }
        });
        $.ajax({
            url: url,
            method: "DELETE"
        }).done(function(){
            window.location = redirect;
        })

    });

});