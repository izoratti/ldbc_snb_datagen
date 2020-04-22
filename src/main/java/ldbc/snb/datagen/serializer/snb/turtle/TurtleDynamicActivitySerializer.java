/* 
 Copyright (c) 2013 LDBC
 Linked Data Benchmark Council (http://www.ldbcouncil.org)
 
 This file is part of ldbc_snb_datagen.
 
 ldbc_snb_datagen is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 ldbc_snb_datagen is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with ldbc_snb_datagen.  If not, see <http://www.gnu.org/licenses/>.
 
 Copyright (C) 2011 OpenLink Software <bdsmt@openlinksw.com>
 All Rights Reserved.
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation;  only Version 2 of the License dated
 June 1991.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.*/
package ldbc.snb.datagen.serializer.snb.turtle;

import com.google.common.collect.ImmutableList;
import ldbc.snb.datagen.dictionary.Dictionaries;
import ldbc.snb.datagen.entities.dynamic.Forum;
import ldbc.snb.datagen.entities.dynamic.messages.Comment;
import ldbc.snb.datagen.entities.dynamic.messages.Photo;
import ldbc.snb.datagen.entities.dynamic.messages.Post;
import ldbc.snb.datagen.entities.dynamic.relations.ForumMembership;
import ldbc.snb.datagen.entities.dynamic.relations.Like;
import ldbc.snb.datagen.hadoop.writer.HdfsWriter;
import ldbc.snb.datagen.serializer.DynamicActivitySerializer;
import ldbc.snb.datagen.serializer.snb.csv.FileName;
import ldbc.snb.datagen.vocabulary.*;

import java.util.List;

import static ldbc.snb.datagen.serializer.snb.csv.FileName.SOCIAL_NETWORK_ACTIVITY;

public class TurtleDynamicActivitySerializer extends DynamicActivitySerializer<HdfsWriter> implements TurtleSerializer {

    private long membershipId = 0;
    private long likeId = 0;

    @Override
    public List<FileName> getFileNames() {
        return ImmutableList.of(SOCIAL_NETWORK_ACTIVITY);
    }

    @Override
    public void writeFileHeaders() { }

    protected void serialize(final Forum forum) {

        StringBuffer result = new StringBuffer(12000);

        String forumPrefix = SN.getForumURI(forum.getId());
        Turtle.addTriple(result, true, false, forumPrefix, RDF.type, SNVOC.Forum);

        Turtle.addTriple(result, false, false, forumPrefix, SNVOC.id,
                         Turtle.createDataTypeLiteral(Long.toString(forum.getId()), XSD.Long));

        Turtle.addTriple(result, false, false, forumPrefix, SNVOC.title,
                         Turtle.createLiteral(forum.getTitle()));
        Turtle.addTriple(result, false, true, forumPrefix, SNVOC.creationDate,
                         Turtle.createDataTypeLiteral(TurtleDateTimeFormat.get().format(forum.getCreationDate()), XSD.DateTime));

        Turtle.createTripleSPO(result, forumPrefix,
                               SNVOC.hasModerator, SN.getPersonURI(forum.getModerator().getAccountId()));

        for (Integer tag : forum.getTags()) {
            String topic = Dictionaries.tags.getName(tag);
            Turtle.createTripleSPO(result, forumPrefix, SNVOC.hasTag, SNTAG.fullPrefixed(topic));
        }
        writers.get(SOCIAL_NETWORK_ACTIVITY).write(result.toString());
    }

    protected void serialize(final Post post) {

        StringBuffer result = new StringBuffer(2500);

        String prefix = SN.getPostURI(post.getMessageId());

        Turtle.addTriple(result, true, false, prefix, RDF.type, SNVOC.Post);

        Turtle.addTriple(result, false, false, prefix, SNVOC.id,
                         Turtle.createDataTypeLiteral(Long.toString(post.getMessageId()), XSD.Long));

        Turtle.addTriple(result, false, false, prefix, SNVOC.creationDate,
                         Turtle.createDataTypeLiteral(TurtleDateTimeFormat.get().format(post.getCreationDate()), XSD.DateTime));

        Turtle.addTriple(result, false, false, prefix, SNVOC.ipaddress,
                         Turtle.createLiteral(post.getIpAddress().toString()));
        Turtle.addTriple(result, false, false, prefix, SNVOC.browser,
                         Turtle.createLiteral(Dictionaries.browsers.getName(post.getBrowserId())));

        Turtle.addTriple(result, false, false, prefix, SNVOC.content,
                         Turtle.createLiteral(post.getContent()));
        Turtle.addTriple(result, false, true, prefix, SNVOC.length,
                         Turtle.createDataTypeLiteral(Integer.toString(post.getContent().length()), XSD.Int));

        Turtle.createTripleSPO(result, prefix, SNVOC.language,
                               Turtle.createLiteral(Dictionaries.languages.getLanguageName(post.getLanguage())));

        Turtle.createTripleSPO(result, prefix, SNVOC.locatedIn,
                               DBP.fullPrefixed(Dictionaries.places.getPlaceName(post.getCountryId())));

        Turtle.createTripleSPO(result, SN.getForumURI(post.getForumId()), SNVOC.containerOf, prefix);
        Turtle.createTripleSPO(result, prefix, SNVOC.hasCreator, SN.getPersonURI(post.getAuthor().getAccountId()));

        for (Integer tag : post.getTags()) {
            String topic = Dictionaries.tags.getName(tag);
            Turtle.createTripleSPO(result, prefix, SNVOC.hasTag, SNTAG.fullPrefixed(topic));
        }
        writers.get(SOCIAL_NETWORK_ACTIVITY).write(result.toString());
    }

    protected void serialize(final Comment comment) {
        StringBuffer result = new StringBuffer(2000);

        String prefix = SN.getCommentURI(comment.getMessageId());

        Turtle.addTriple(result, true, false, prefix, RDF.type, SNVOC.Comment);

        Turtle.addTriple(result, false, false, prefix, SNVOC.id,
                         Turtle.createDataTypeLiteral(Long.toString(comment.getMessageId()), XSD.Long));

        Turtle.addTriple(result, false, false, prefix, SNVOC.creationDate,
                         Turtle.createDataTypeLiteral(TurtleDateTimeFormat.get().format(comment.getCreationDate()), XSD.DateTime));
        Turtle.addTriple(result, false, false, prefix, SNVOC.ipaddress,
                         Turtle.createLiteral(comment.getIpAddress().toString()));
        Turtle.addTriple(result, false, false, prefix, SNVOC.browser,
                         Turtle.createLiteral(Dictionaries.browsers.getName(comment.getBrowserId())));
        Turtle.addTriple(result, false, false, prefix, SNVOC.content,
                         Turtle.createLiteral(comment.getContent()));
        Turtle.addTriple(result, false, true, prefix, SNVOC.length,
                         Turtle.createDataTypeLiteral(Integer.toString(comment.getContent().length()), XSD.Int));

        String replied = (comment.replyOf() == comment.postId()) ? SN.getPostURI(comment.postId()) :
                SN.getCommentURI(comment.replyOf());
        Turtle.createTripleSPO(result, prefix, SNVOC.replyOf, replied);
        Turtle.createTripleSPO(result, prefix, SNVOC.locatedIn,
                               DBP.fullPrefixed(Dictionaries.places.getPlaceName(comment.getCountryId())));

        Turtle.createTripleSPO(result, prefix, SNVOC.hasCreator,
                               SN.getPersonURI(comment.getAuthor().getAccountId()));

        for (Integer tag : comment.getTags()) {
            String topic = Dictionaries.tags.getName(tag);
            Turtle.createTripleSPO(result, prefix, SNVOC.hasTag, SNTAG.fullPrefixed(topic));
        }
        writers.get(SOCIAL_NETWORK_ACTIVITY).write(result.toString());
    }

    protected void serialize(final Photo photo) {
        StringBuffer result = new StringBuffer(2500);

        String prefix = SN.getPostURI(photo.getMessageId());
        Turtle.addTriple(result, true, false, prefix, RDF.type, SNVOC.Post);

        Turtle.addTriple(result, false, false, prefix, SNVOC.id,
                         Turtle.createDataTypeLiteral(Long.toString(photo.getMessageId()), XSD.Long));

        Turtle.addTriple(result, false, false, prefix, SNVOC.hasImage, Turtle.createLiteral(photo.getContent()));
        Turtle.addTriple(result, false, false, prefix, SNVOC.ipaddress,
                         Turtle.createLiteral(photo.getIpAddress().toString()));
        Turtle.addTriple(result, false, false, prefix, SNVOC.browser,
                         Turtle.createLiteral(Dictionaries.browsers.getName(photo.getBrowserId())));
        Turtle.addTriple(result, false, true, prefix, SNVOC.creationDate,
                         Turtle.createDataTypeLiteral(TurtleDateTimeFormat.get().format(photo.getCreationDate()), XSD.DateTime));

        Turtle.createTripleSPO(result, prefix, SNVOC.hasCreator, SN.getPersonURI(photo.getAuthor().getAccountId()));
        Turtle.createTripleSPO(result, SN.getForumURI(photo.getForumId()), SNVOC.containerOf, prefix);
        Turtle.createTripleSPO(result, prefix, SNVOC.locatedIn,
                               DBP.fullPrefixed(Dictionaries.places.getPlaceName(photo.getCountryId())));

        for (Integer tag : photo.getTags()) {
            String topic = Dictionaries.tags.getName(tag);
            Turtle.createTripleSPO(result, prefix, SNVOC.hasTag, SNTAG.fullPrefixed(topic));
        }
        writers.get(SOCIAL_NETWORK_ACTIVITY).write(result.toString());
    }

    protected void serialize(final ForumMembership membership) {
        String memberhipPrefix = SN.getMembershipURI(SN.formId(membershipId));
        String forumPrefix = SN.getForumURI(membership.getForumId());
        StringBuffer result = new StringBuffer(19000);
        Turtle.createTripleSPO(result, forumPrefix, SNVOC.hasMember, memberhipPrefix);

        Turtle.addTriple(result, true, false, memberhipPrefix, SNVOC.hasPerson, SN
                .getPersonURI(membership.getPerson().getAccountId()));
        Turtle.addTriple(result, false, true, memberhipPrefix, SNVOC.creationDate,
                         Turtle.createDataTypeLiteral(TurtleDateTimeFormat.get().format(membership.getCreationDate()), XSD.DateTime));
        membershipId++;
        writers.get(SOCIAL_NETWORK_ACTIVITY).write(result.toString());
    }

    protected void serialize(final Like like) {
        StringBuffer result = new StringBuffer(2500);
        long id = SN.formId(likeId);
        String likePrefix = SN.getLikeURI(id);
        Turtle.createTripleSPO(result, SN.getPersonURI(like.getPerson()),
                               SNVOC.like, likePrefix);

        if (like.getType() == Like.LikeType.POST || like.getType() == Like.LikeType.PHOTO) {
            String prefix = SN.getPostURI(like.getMessageId());
            Turtle.addTriple(result, true, false, likePrefix, SNVOC.hasPost, prefix);
        } else {
            String prefix = SN.getCommentURI(like.getMessageId());
            Turtle.addTriple(result, true, false, likePrefix, SNVOC.hasComment, prefix);
        }
        Turtle.addTriple(result, false, true, likePrefix, SNVOC.creationDate,
                         Turtle.createDataTypeLiteral(TurtleDateTimeFormat.get().format(like.getCreationDate()), XSD.DateTime));
        likeId++;
        writers.get(SOCIAL_NETWORK_ACTIVITY).write(result.toString());
    }

}
