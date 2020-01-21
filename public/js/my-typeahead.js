/*
* DEFINING HERE HOW TYPEAHEAD MAKE REQUESTS TO ELASTICSEARCH TO AUTOCOMPLETE SEARCHES
*/

var searchResultsName = new Bloodhound({
    datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name'),
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    // identify: function(product) { return product.name; },
    remote: {
        url: '../es/typeahead/name/%SEARCH',
        wildcard: '%SEARCH'
    }
});

var searchResultsDescription = new Bloodhound({
    datumTokenizer: Bloodhound.tokenizers.obj.whitespace('description'),
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    // identify: function(product) { return product.name + " | " + product.description; },
    remote: {
        url: '../es/typeahead/des/%SEARCH',
        wildcard: '%SEARCH'
    }
});


$('#search').typeahead(
    {
        hint: true,
        highlight: true,
        minLength: 0
    },
    {
        name: 'elasticsearch-name-query',
        display: 'name',
        source: searchResultsName,
        templates: {
            header: '<div class="product-field">in \"name\"</div>'
        },
        limit: 10 // limit => max number of products to display in the suggestion list
        /*
        IMPORTANT : 'limit' is placed here because of a bug in Typeahead when setting a limit below 4.
        Correcting it needs me to set its value to 3-4 more than what I need.
        see for more: https://stackoverflow.com/questions/30213747/typeahead-js-bloodhound-minlength-not-working/30330790#30330790
        */
    },
    {
        name: 'elasticsearch-description-query',
        display: 'description',
        source: searchResultsDescription,
        templates: {
            header: '<div class="product-field">in \"description\"</div>'
        },
        limit: 10
    }
);