var api_base = 'http://localhost:9080/aprox/api/1.0';

var $tabs;
$(function(){
    $tabs = $('#tabs').tabs({
      // tabTemplate: '<li><a href="#{href}"><span>#{label}</span></a></li>',
      // collapsible: true,
    });
});

var panels_loaded = [];
function addMenuItem(menu, link_name, title, id, page) {
	var link_list = $(menu).find( 'ul' );
	$('<a/>').addClass('menu-link').attr('menu-id', id).attr('href', '#admin-workspaces').text(link_name).wrap('<li></li>').appendTo( link_list );
}

function addTab(name, page) {
  $tabs.tabs('add', page, name);
}

function notice( id, message ){
  $('#notices ul').append( '<li class="ui-icon-info" id="notice-' + id + '">' + message + '</li>' );
}

function clearNotice( id ){
  clear_notice( id );
}

function clear_notice( id ){
  // $('#notice-' + id).delay(1000).hide( 'slow', function(){
  //   $(this).detach();
  // });
}

function enable(element){
  $(element).removeAttr('disabled');
}

function disable(element){
  $(element).attr('disabled', true);
}

function renderApiLink(parts) {
  var url = renderApiUrl( parts );
  return '<a target="_new" href="' + url + '">' + url + '</a>';
}

function renderAccessLink(type, name) {
  return renderApiLink([type, name]);
}

function renderAdminLink(type, sub) {
  return renderApiLink(['admin', type, sub]);
}

function renderRemoteLink(url) {
  return '<a target="_new" href="' + url + '">' + url + '</a>';
}

function renderApiUrl(parts) {
  var path = '';
  for (var i = 0; i < parts.length; i++) {
    if ( parts[i] && parts[i] != ''){
      path += '/' + parts[i];
    }
  }

  return api_base + path;
}

function renderAccessUrl(type, name) {
  return renderApiUrl([type, name]);
}

function renderAdminUrl(type, sub) {
  return renderApiUrl(['admin', type, sub]);
}

function addOption( select, options ){ // options = {name: something, key: 'repository:something', name_prefix: bar, name_suffix: baz}
  var prefix = options.name_prefix ? options.name_prefix : '';
  var suffix = options.name_suffix ? options.name_suffix : '';

  $(select).append( '<option name="' + options.key + '">' + prefix + ' ' + options.name + ' ' + suffix + '</option>' );
}

function deleteOption( select, key ){
  $(select).find('option[name="' + key + '"]').detach();
}

function loadOptions( select, option_sets ){ // option_sets = [ {type: foo, name_prefix: bar, name_suffix: baz}, ...]
  var reqs = [];
  $.each( option_sets, function( i, options ){
    var listUrl = renderAdminUrl( options.type, 'list' );

    reqs.push(
      $.getJSON( listUrl, function(data)
      {
        var prefix = options.name_prefix ? options.name_prefix : '';
        var suffix = options.name_suffix ? options.name_suffix : '';
        
        $.each(data.items, function(index, value){
          $(select).append( '<option name="' + value.key + '">' + prefix + ' ' + value.name + ' ' + suffix + '</option>' );
        });
      })
      .error( function()
      {
        notice( 'loading-options-' + options.type + '-failed', "Failed to load options of type: " + options.type );
        clear_notice( 'loading-options-' + options.type + '-failed' );
        clear_notice( 'loading-options-' + options.type );
      })
    );
  });
  
  return reqs;
}

function postJSON( url, data, notice ){
  ajaxPost( url, data, notice );
}

function ajaxPost( url, data, notice ){
  $.ajax({
    type: 'POST',
    accepts: 'application/json',
    contentType: 'application/json',
    url: url,
    data: JSON.stringify( data ),
  }).done(function(message){
    clear_notice( notice );
  });
}

function ajaxDelete( url, notice ){
  $.ajax({
    type: 'DELETE',
    accepts: 'application/json',
    contentType: 'application/json',
    url: url,
  }).done(function(message){
    clear_notice( notice );
  });
}

function selectRow( row, table ){
  if ( !$(row).hasClass('row_selected') ) {
      $(table).find('tr.row_selected').removeClass('row_selected');
      $(row).addClass('row_selected');
  }
}

function clearSelectedRow( table ){
  $(table).find('tr.row_selected').removeClass('row_selected');
}


$('a.menu-link').live('click', function(evt){
	var id = $(this).attr('menu-id');
  if (!panels_loaded[id]) {
    addTab(name, page);
    panels_loaded[id] = true;
  }
});

