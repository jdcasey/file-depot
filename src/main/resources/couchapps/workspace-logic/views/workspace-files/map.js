function (doc){
	if ( doc.doctype == 'file' ){
		emit( doc.workspace, {'_id': doc._id} );
	}
}