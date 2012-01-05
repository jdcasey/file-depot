package org.commonjava.web.fd.model;

import static org.commonjava.couch.util.IdUtils.namespaceId;

import java.util.Date;

import org.commonjava.couch.model.AbstractCouchDocWithAttachments;
import org.commonjava.couch.model.Attachment;
import org.commonjava.couch.model.AttachmentInfo;
import org.commonjava.couch.model.DenormalizedCouchDoc;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WorkspaceFile
    extends AbstractCouchDocWithAttachments
    implements DenormalizedCouchDoc
{

    public static final String NAMESPACE = "file";

    @SerializedName( "workspace" )
    private String workspaceName;

    @SerializedName( "file" )
    private String fileName;

    @Expose( serialize = false, deserialize = false )
    private Attachment inboundAttachment;

    @SerializedName( "last_modified" )
    private long lastModified;

    @Expose( deserialize = false )
    private final String doctype = NAMESPACE;

    WorkspaceFile()
    {
    }

    public WorkspaceFile( final String workspace, final String name, final Attachment inboundAttachment,
                          final Date lastModified )
    {
        this.workspaceName = workspace;
        this.fileName = name;
        this.inboundAttachment = inboundAttachment;
        this.lastModified = lastModified.getTime();
        calculateDenormalizedFields();
    }

    public WorkspaceFile( final String workspace, final String name, final Attachment inboundAttachment,
                          final long lastModified )
    {
        this.workspaceName = workspace;
        this.fileName = name;
        this.inboundAttachment = inboundAttachment;
        this.lastModified = lastModified;
        calculateDenormalizedFields();
    }

    public String getDoctype()
    {
        return doctype;
    }

    public Date getLastModifiedDate()
    {
        return new Date( lastModified );
    }

    public String getContentType()
    {
        if ( inboundAttachment != null )
        {
            return inboundAttachment.getContentType();
        }

        final AttachmentInfo att = getAttachments().get( 0 );
        return att.getContentType();
    }

    public long getContentLength()
    {
        if ( inboundAttachment != null )
        {
            return inboundAttachment.getContentLength();
        }

        final AttachmentInfo att = getAttachments().get( 0 );
        return att.getContentLength();
    }

    @Override
    public void calculateDenormalizedFields()
    {
        setCouchDocId( namespaceId( NAMESPACE, workspaceName, fileName ) );
    }

    public String getWorkspaceName()
    {
        return workspaceName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public Attachment getInboundAttachment()
    {
        return inboundAttachment;
    }

    void setWorkspaceName( final String workspaceName )
    {
        this.workspaceName = workspaceName;
    }

    void setFileName( final String fileName )
    {
        this.fileName = fileName;
    }

    void setInboundAttachment( final Attachment inboundAttachment )
    {
        this.inboundAttachment = inboundAttachment;
    }

    public long getLastModified()
    {
        return lastModified;
    }

    void setLastModified( final long lastModified )
    {
        this.lastModified = lastModified;
    }

    @Override
    public String toString()
    {
        return String.format( "WorkspaceFile [workspace=%s, file=%s]", workspaceName, fileName );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ( ( fileName == null ) ? 0 : fileName.hashCode() );
        result = prime * result + ( ( workspaceName == null ) ? 0 : workspaceName.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( !super.equals( obj ) )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final WorkspaceFile other = (WorkspaceFile) obj;
        if ( fileName == null )
        {
            if ( other.fileName != null )
            {
                return false;
            }
        }
        else if ( !fileName.equals( other.fileName ) )
        {
            return false;
        }
        if ( workspaceName == null )
        {
            if ( other.workspaceName != null )
            {
                return false;
            }
        }
        else if ( !workspaceName.equals( other.workspaceName ) )
        {
            return false;
        }
        return true;
    }

}
