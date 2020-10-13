/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.dex.controllers.graphics.sprites.retangles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.graphics.GraphicsCanvasController;
import org.opentravel.dex.controllers.graphics.sprites.DexSprite;
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmProperties.OtmProperty;

import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * Graphics utility for containing facet regions.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class FacetRectangle extends Rectangle {
    private static Log log = LogFactory.getLog( FacetRectangle.class );

    public static final double FACET_MARGIN = 5;
    public static final double FACET_OFFSET = 8;

    // Left Margin offsets per facet type
    public static final double ID_OFFSET = FACET_OFFSET;
    public static final double SHARED_OFFSET = ID_OFFSET;
    public static final double QUERY_OFFSET = ID_OFFSET;
    public static final double UPDATE_OFFSET = ID_OFFSET;
    public static final double SUMMARY_OFFSET = ID_OFFSET + FACET_OFFSET;
    public static final double CHOICE_OFFSET = SUMMARY_OFFSET;
    public static final double DETAIL_OFFSET = SUMMARY_OFFSET + FACET_OFFSET;
    public static final double CUSTOM_OFFSET = DETAIL_OFFSET;

    private static final Paint FACET_COLOR = Color.ANTIQUEWHITE;


    /**
     * Render methods that create rectangles may set the event to run if the implement this interface.
     * <p>
     * Example: r.setOnMouseClicked( e -> manager.remove( this ) );
     */
    public abstract interface RectangleEventHandler {
        public void onRectangleClick(MouseEvent e);
    }


    private OtmFacet<?> facet = null;
    private OtmContextualFacet cFacet = null;
    private DexSprite<OtmLibraryMember> parent;
    private Font font;
    private String label;
    private Image icon;
    private List<OtmObject> children;

    public FacetRectangle(OtmFacet<?> facet, DexSprite<OtmLibraryMember> parentSprite, double width) {
        super( 0, 0, width, 0 );
        this.facet = facet;
        this.parent = parentSprite;
        this.icon = facet.getIcon();
        this.label = facet.getName();
        this.children = facet.getChildren();

        if (parentSprite != null)
            this.font = parentSprite.getFont();
        else
            this.font = GraphicsCanvasController.DEFAULT_FONT;
        // Compute the size
        draw( null, font );
    }

    public FacetRectangle(OtmContextualFacet member, DexSprite<OtmLibraryMember> parentSprite, double width) {
        super( 0, 0, width, 0 );
        // this.facet = facet;
        this.cFacet = member;
        this.parent = parentSprite;
        this.icon = member.getIcon();
        this.label = member.getName();
        this.children = member.getChildren();

        if (parentSprite != null)
            this.font = parentSprite.getFont();
        else
            this.font = GraphicsCanvasController.DEFAULT_FONT;
        // Compute the size
        draw( null, font );
    }

    public FacetRectangle(OtmValueWithAttributes member, DexSprite<OtmLibraryMember> parentSprite, double width) {
        super( 0, 0, width, 0 );
        // this.facet = facet;
        // this.cFacet = member;
        this.parent = parentSprite;
        this.icon = member.getIcon();
        this.label = member.getName();
        this.icon = null;
        this.label = null;
        this.children = member.getChildren();

        if (parentSprite != null)
            this.font = parentSprite.getFont();
        else
            this.font = GraphicsCanvasController.DEFAULT_FONT;
        // Compute the size
        draw( null, font );
    }

    /**
     * Draw the facet.
     * 
     * @param gc
     * @param filled
     */
    @Override
    public Rectangle draw(GraphicsContext gc, boolean filled) {
        if (gc != null) {
            Paint savedColor = gc.getFill();
            gc.setFill( FACET_COLOR );

            super.draw( gc, false ); // draw outline
            if (filled)
                super.draw( gc, true ); // Draw fill

            draw( gc, font );

            gc.setFill( savedColor );
        }
        return this;
    }


    private Rectangle draw(GraphicsContext gc, Font font) {
        boolean compute = gc == null;
        height = 0;

        // Label
        if (label != null) {
            Rectangle lRect = GraphicsUtils.drawLabel( label, icon, gc, font, x, y );
            height = lRect.getHeight();
            width = compute && lRect.getWidth() > width ? lRect.getWidth() : width;
        }

        // Properties
        PropertyRectangle pRect = null;
        double py = y + height;
        double px = x + PropertyRectangle.PROPERTY_OFFSET;
        if (!children.isEmpty()) {
            for (OtmObject c : children) {
                if (c instanceof OtmProperty) {
                    pRect = new PropertyRectangle( (OtmProperty) c, parent, font, width );
                    pRect.set( px, py );
                    pRect.draw( gc, true );

                    height += pRect.getHeight();
                    width = compute && pRect.getWidth() > width ? pRect.getWidth() : width;
                    py += pRect.getHeight();
                }
            }
            // Draw vertical line
            if (pRect != null && !compute) {
                px = px + PropertyRectangle.PROPERTY_MARGIN - 1;
                double ly = y + height - 2 * PropertyRectangle.PROPERTY_MARGIN - 1;
                gc.strokeLine( px, y + pRect.getHeight(), px, ly );
            }
        }
        // log.debug( "Drew/sized " + this );
        // super.draw( gc, false ); // debug
        return this;
    }

    @Override
    public String toString() {
        return "Facet: " + facet + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}