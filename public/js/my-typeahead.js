/*
* DEFINING HERE HOW TYPEAHEAD MAKE REQUESTS TO ELASTICSEARCH TO AUTOCOMPLETE SEARCHES
*/

var substringMatcher = function(datasetStrings) {

    return function findMatches(query, cb) {
        var matches = [];                                      // this array will be populated with the substrings matching our request
        var substringRegex = new RegExp(query, 'i');    // regex used to determine whether a string contains the substring 'query'
                                                             //  the flag 'i' enable 'ignoreCase' when comparing using the regex

        $.each(datasetStrings, function(i, datasetString){     // here 'i' might be for 'index', a variable not used here anyway
            if(substringRegex.test(datasetString)){                     // if the query typed match the current evaluated string from the dataset then it's added to the matches array
                matches.push(datasetString);
            }
        });
        cb(matches);
    };

};

var searchResult = new Bloodhound({
    datumTokenizer: Bloodhound.tokenizers.obj.whitespace('ean','name','description'),
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    // identify: function(product) { return product; },
    remote: {
        url: '../es/typeahead/%QUERY',
        wildcard: '%QUERY'
    }
    /*local: ['Alabama', 'Alaska', 'Arizona', 'Arkansas', 'California',
        'Colorado', 'Connecticut', 'Delaware', 'Florida', 'Georgia', 'Hawaii',
        'Idaho', 'Illinois', 'Indiana', 'Iowa', 'Kansas', 'Kentucky', 'Louisiana',
        'Maine', 'Maryland', 'Massachusetts', 'Michigan', 'Minnesota',
        'Mississippi', 'Missouri', 'Montana', 'Nebraska', 'Nevada', 'New Hampshire',
        'New Jersey', 'New Mexico', 'New York', 'North Carolina', 'North Dakota',
        'Ohio', 'Oklahoma', 'Oregon', 'Pennsylvania', 'Rhode Island',
        'South Carolina', 'South Dakota', 'Tennessee', 'Texas', 'Utah', 'Vermont',
        'Virginia', 'Washington', 'West Virginia', 'Wisconsin', 'Wyoming'
    ]*/
});




$('#inputTypeahead').typeahead({
    hint: true,
    highlight: true,
    minLength: 1
},
{
    name: 'elasticsearch-query',
    // limit: 6,
    display: 'name',
    source: searchResult
});