package org.cocos2d.grid;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.cocos2d.types.CCVertex3D;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.ccGridSize;
import org.cocos2d.types.ccQuad3;

import com.badlogic.gdx.utils.BufferUtils;


/**
 CCGrid3D is a 3D grid implementation. Each vertex has 3 dimensions: x,y,z
 */
public class CCGrid3D extends CCGridBase {
    protected ShortBuffer indices;
    
	protected FloatBuffer texCoordinates;
	protected FloatBuffer vertices;
	protected FloatBuffer originalVertices;
	protected FloatBuffer mVertexBuffer;

    public CCGrid3D(ccGridSize gSize) {
        super(gSize);
        calculateVertexPoints();
    }

    float[] vertarray = new float[0];
    int i = 0;
    FloatBuffer temp1;
    FloatBuffer temp2;
    boolean fbswitch = true;
    @Override
    public void blit(GL10 gl) {
        // Default GL states: GL_TEXTURE_2D, GL_VERTEX_ARRAY, GL_COLOR_ARRAY, GL_TEXTURE_COORD_ARRAY
        // Needed states: GL_TEXTURE_2D, GL_VERTEX_ARRAY, GL_TEXTURE_COORD_ARRAY
        // Unneeded states: GL_COLOR_ARRAY
	    gl.glDisableClientState(GL10.GL_COLOR_ARRAY);	
        
	    /** there's no need to create a new buffer on every blit,
	     *  simply switching back and forth to allow the GLThread
	     *  to finish reading one while we load the other is
	     *  sufficient.
	     */
        if (fbswitch) {
        	if (temp1 == null)
        		temp1 = BufferUtils.newFloatBuffer(vertices.limit()*3*4);
        	
        	mVertexBuffer = temp1;
        } else {
        	if (temp2 == null) 
        		temp2 = BufferUtils.newFloatBuffer(vertices.limit()*3*4);
      
        	mVertexBuffer = temp2;
        }
        
        fbswitch = !fbswitch;
        
        mVertexBuffer.clear();          
        mVertexBuffer.position(0);
        
        BufferUtils.copy(vertices.array(), mVertexBuffer, 0);
        mVertexBuffer.limit(mVertexBuffer.capacity());
        
//        for (i = 0; i < vertices.limit(); i+=3) {
//            mVertexBuffer.put(vertices.get(i)).put(vertices.get(i+1)).put(vertices.get(i+2));
//        }
//        
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        // gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texCoordinates);
        indices.position(0);

        gl.glDrawElements(GL10.GL_TRIANGLES, gridSize_.x * gridSize_.y * 6, GL10.GL_UNSIGNED_SHORT, indices);

        // restore GL default state
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    }
	
    @Override
    public void calculateVertexPoints() {
        float width = (float)texture_.pixelsWide();
        float height = (float)texture_.pixelsHigh();
        // float imageH = texture_.getContentSize().height;
	
        int x, y, i;

        /** I decided to use a regular float buffer for vertices since it is very rarely written to
         *  but often read from.  The trade off I observed from direct vs normal buffers it that:
         *  direct = very fast write, slow (and limited!) read
         *  normal = average write (for single float) / EXTREMELY slow write (bulk), 
         *  		 average read time (or instant with buffer.array(), which is impossible with direct buffer)
         */
        vertices = FloatBuffer.allocate(ccQuad3.size * (gridSize_.x + 1) * (gridSize_.y + 1) * 4);
        originalVertices = BufferUtils.newFloatBuffer(ccQuad3.size * (gridSize_.x + 1) * (gridSize_.y + 1) * 4);
        texCoordinates = BufferUtils.newFloatBuffer(2 * (gridSize_.x + 1) * (gridSize_.y + 1) * 4);
        indices = BufferUtils.newShortBuffer(6 * (gridSize_.x + 1) * (gridSize_.y + 1) * 2);
        
        int idx;
        for (y = 0; y < (gridSize_.y + 1); y++) {
            for (x = 0; x < (gridSize_.x + 1); x++) {
            	idx = (y * (gridSize_.x + 1)) + x;
                vertices.put(idx * 3 + 0, -1);
                vertices.put(idx * 3 + 1, -1);
                vertices.put(idx * 3 + 2, -1);
                vertices.put(idx * 2 + 0, -1);
                vertices.put(idx * 2 + 1, -1);
                
            }
        }
        vertices.position(0);

        float x1, x2, y1, y2 = 0;
        short a, b, c, d = 0;
        CCVertex3D e, f, g, h = null;
        CCVertex3D[] l2 = new CCVertex3D[4];
        int[] tex1 = new int[4];
        int[] l1 = new int[4];
        CGPoint[] tex2 = new CGPoint[4];
        for (x = 0; x < gridSize_.x; x++) {
            for (y = 0; y < gridSize_.y; y++) {
            	idx = (y * gridSize_.x) + x;

                x1 = x * step_.x;
                x2 = x1 + step_.x;
                y1 = y * step_.y;
                y2 = y1 + step_.y;

                a = (short) (x * (getGridHeight() + 1) + y);
                b = (short) ((x + 1) * (getGridHeight() + 1) + y);
                c = (short) ((x + 1) * (getGridHeight() + 1) + (y + 1));
                d = (short) (x * (getGridHeight() + 1) + (y + 1));

               	indices.position(6 * idx);
               	indices.put(new short[]  {a, b, d, b, c, d}, 0, 6);

                l1[0] = a * 3;
                l1[1] = b * 3;
                l1[2] = c * 3;
                l1[3] = d * 3;
                
                e = new CCVertex3D(x1, y1, 0);
                f = new CCVertex3D(x2, y1, 0);
                g = new CCVertex3D(x2, y2, 0);
                h = new CCVertex3D(x1, y2, 0);

                l2[0] = e;
                l2[1] = f;
                l2[2] = g;
                l2[3] = h;

                tex1[0] = a * 2;
                tex1[1] = b * 2;
                tex1[2] = c * 2;
                tex1[3] = d * 2;
                
                tex2[0] = CGPoint.ccp(x1, y1);
                tex2[1] = CGPoint.ccp(x2, y1);
                tex2[2] = CGPoint.ccp(x2, y2);
                tex2[3] = CGPoint.ccp(x1, y2);

                for (i = 0; i < 4; i++) {
                    vertices.put(l1[i] + 0, l2[i].x);
                    vertices.put(l1[i] + 1, l2[i].y);
                    vertices.put(l1[i] + 2, l2[i].z);

                	BufferUtils.copy(new float[] {tex2[i].x / width, tex2[i].y / height}, texCoordinates, tex1[i]);
                }
            }
        }
        indices.position(0);
        vertices.position(0);
        texCoordinates.position(0);

        originalVertices.put(vertices);
        originalVertices.position(0);
    }

    /** returns the vertex at a given position */
    public CCVertex3D vertex(ccGridSize pos) {
        int index = (pos.x * (gridSize_.y + 1) + pos.y) * 3;
        CCVertex3D vert = new CCVertex3D(vertices.get(index + 0), vertices.get(index + 1), vertices.get(index + 2));

        return vert;
    }

    /** returns the original (non-transformed) vertex at a given position */
    public CCVertex3D originalVertex(ccGridSize pos) {
        int index = (pos.x * (gridSize_.y + 1) + pos.y) * 3;
        CCVertex3D vert = new CCVertex3D(originalVertices.get(index + 0), originalVertices.get(index + 1), originalVertices.get(index + 2));

        return vert;
    }

    /** sets a new vertex at a given position */
    public void setVertex(ccGridSize pos, CCVertex3D vertex) {
        int index = (pos.x * (gridSize_.y + 1) + pos.y) * 3;
        vertices.put(index + 0, vertex.x);
        vertices.put(index + 1, vertex.y);
        vertices.put(index + 2, vertex.z);
    }

    @Override
    public void reuse(GL10 gl) {
        if (reuseGrid_ > 0) {
//            memcpy(originalVertices, vertices, (getGridWidth()+1)*(getGridHeight()+1)*sizeof(CCVertex3D));
            reuseGrid_--;
        }

    }
}


