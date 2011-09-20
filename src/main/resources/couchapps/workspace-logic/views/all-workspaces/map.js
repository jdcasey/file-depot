function (doc){
	if ( doc.doctype == 'workspace' ){
		emit( doc._id, doc._rev );
	}
}