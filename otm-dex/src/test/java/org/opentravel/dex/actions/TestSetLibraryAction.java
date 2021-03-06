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

package org.opentravel.dex.actions;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.io.IOException;
import java.util.List;

/**
 * Verifies the functions of the <code>new library member action</code> class.
 */
public class TestSetLibraryAction {

    private static Log log = LogFactory.getLog( TestSetLibraryAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
        lib = staticModelManager.add( new TLLibrary() );
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" ); // Tested in buildOtm()
    }


    @Test
    public void testMultipleMembers() {
        // DexFullActionManager fullMgr = new DexFullActionManager( null );
        // OtmModelManager mgr = new OtmModelManager( fullMgr, null );
        // OtmLibrary lib = mgr.add( new TLLibrary() );
        // DexActionManager actionManager = lib.getActionManager();
        // assertTrue( "Given", actionManager instanceof DexFullActionManager );
        // assertTrue( actionManager.getQueueSize() == 0 );
        // OtmBusinessObject testBO = TestBusiness.buildOtm( mgr, "TestBusinessObject" );
        // assertTrue( "Given", testBO.getModelManager() == mgr );
        // lib.add( testBO );
        // assertTrue( "Given", testBO.getLibrary() == lib );
        //
        // Object result = null;
        // OtmLibraryMember member = null;
        // for (LibraryMemberType type : LibraryMemberType.values()) {
        // // FIXED - DexActions no longer only uses subject's AM, not the caller
        // result = actionManager.run( DexActions.NEWLIBRARYMEMBER, testBO, type );
        // assertTrue( "Must create library member.", result instanceof OtmLibraryMember );
        // member = (OtmLibraryMember) result;
        // assertTrue( "Must only have one action in queue.", actionManager.getQueueSize() == 1 );
        // assertTrue( "Model manager must contain new member.", mgr.contains( member ) );
        //
        // // When - undo
        // actionManager.undo();
        // assertFalse( "Model manager must not contain new member.", mgr.contains( member ) );
        // assertTrue( "Action queue must be empty.", actionManager.getQueueSize() == 0 );
        // }
        // assertTrue( "Action queue must be empty.", actionManager.getQueueSize() == 0 );
        // log.debug( "New Library Member Test complete." );
    }


    @Test
    public void testSimpleChangeLibrary()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );
        OtmLibrary lib1 = mgr.add( new TLLibrary() );
        assertTrue( "Given", lib1.isEditable() );

        OtmLibrary lib2 = mgr.add( new TLLibrary() );
        assertTrue( "Given", lib2.isEditable() );

        DexActionManager actionManager = lib1.getActionManager();
        assertTrue( "Given", actionManager instanceof DexFullActionManager );
        assertTrue( "Given", actionManager.getQueueSize() == 0 );

        OtmBusinessObject member = TestBusiness.buildOtm( lib1, "TestBusinessObject" );
        assertTrue( "Given", member.getModelManager() == mgr );
        assertTrue( "Given", member.getLibrary() == lib1 );

        // Action handler with subject set to the member
        SetLibraryAction setLibraryHandler =
            (SetLibraryAction) DexActions.getAction( DexActions.SETLIBRARY, member, actionManager );
        assertTrue( "Given", setLibraryHandler != null );

        // When
        OtmLibrary result = setLibraryHandler.doIt( lib2 );
        // Then - member is in the new library (lib2)
        assertTrue( "doIt did not return the new library.", result == lib2 );
        assertTrue( "Then member's library is library 2.", member.getLibrary() == lib2 );
        assertTrue( "New library must contain member.", lib2.contains( member ) );
        assertTrue( "New TL Library must contain named member.",
            lib2.getTL().getNamedMember( member.getName() ) != null );

        // Then - member is not in the original library (lib1)
        assertTrue( "Original library must not contain member.", !lib1.contains( member ) );
        assertTrue( "Original TL Library must not contain named member.",
            lib1.getTL().getNamedMember( member.getName() ) == null );

        // When
        setLibraryHandler.undoIt();
        assertTrue( "Then member's library must be library 1.", member.getLibrary() == lib1 );
    }

    @Test
    public void testSetWhenAssigned()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );
        OtmLibrary lib1 = TestLibrary.buildOtm( mgr, "Namespace1", "p1", "Library1" );
        log.debug( "Lib 1 name is: " + lib1.getFullName() );
        OtmLibrary lib2 = TestLibrary.buildOtm( mgr, "Namespace2", "p2", "Library2" );
        DexActionManager actionManager = lib1.getActionManager();

        // Given - a member with element to assign the moved object.
        OtmBusinessObject member = TestBusiness.buildOtm( lib1, "TestBusinessObject" );
        assertTrue( "Given", member.getLibrary() == lib1 );
        OtmElement<?> element = TestBusiness.getElement( member );
        assertTrue( "Given", element != null );
        assertTrue( "Given", element.getOwningMember() == member );
        assertTrue( "Given", element.getLibrary() == lib1 );

        // Given - the object to move assigned to the member's element
        OtmCore assignedType = TestCore.buildOtm( lib1, "AssignedCore" );
        element.setAssignedType( assignedType );
        assertTrue( "Given", element.getTL().getType() == assignedType.getTL() );
        assertTrue( "Given", element.getAssignedType() == assignedType );
        assertTrue( "Given - property set.", element.assignedTypeProperty().get().equals( assignedType.getName() ) );
        log.debug(
            element.getAssignedType().getNameWithPrefix() + "  property = " + element.assignedTypeProperty().get() );
        List<OtmLibraryMember> initialFoundUsers = mgr.findUsersOf( assignedType );

        // Given - The action to test with subject set to assignedType object
        SetLibraryAction setLibraryHandler =
            (SetLibraryAction) DexActions.getAction( DexActions.SETLIBRARY, assignedType, actionManager );
        assertTrue( "Given", setLibraryHandler != null );

        // When
        setLibraryHandler.doIt( lib2 );

        // Then - library must be lib2
        assertTrue( "Then - assignedType's library be library 2.", assignedType.getLibrary() == lib2 );
        assertTrue( "Then - assignedType's TL's owning library must be tlLib 2.",
            assignedType.getTL().getOwningLibrary() == lib2.getTL() );
        assertTrue( "Then - assignedType's library's TLlib must be tlLib 2.",
            assignedType.getLibrary().getTL() == lib2.getTL() );

        // Then - must still be assigned to element
        assertTrue( "Then - element must still be assigned to type.", element.getAssignedType() == assignedType );
        assertTrue( "Then - element's TL type must still be assignedType's TL.",
            element.getTL().getType() == assignedType.getTL() );

        // Then - the whereUsed list must still contain member
        List<OtmLibraryMember> foundUsers = mgr.findUsersOf( assignedType );
        List<OtmLibraryMember> wu = assignedType.getWhereUsed();
        assertTrue( "Then - the whereUsed list must still contain member.", wu.contains( member ) );

        // Then - the FX Property must have lib2's prefix
        log.debug(
            element.getAssignedType().getNameWithPrefix() + "  property = " + element.assignedTypeProperty().get() );
        assertTrue( "Then - property p2 prefix.",
            element.assignedTypeProperty().get().contains( assignedType.getPrefix() ) );

        // When
        setLibraryHandler.undoIt();
        assertTrue( "When assignedType's library is library 1.", assignedType.getLibrary() == lib1 );
        assertTrue( "When assignedType;s TLlib is tlLib 1.", assignedType.getTL().getOwningLibrary() == lib1.getTL() );
        assertTrue( "When assignedType;s TLlib is tlLib 1.", assignedType.getLibrary().getTL() == lib1.getTL() );
        assertTrue( "Then - TL type must still be assignedType's TL.",
            element.getTL().getType() == assignedType.getTL() );
        assertTrue( "Then - element must still be assigned to type.", element.getAssignedType() == assignedType );
        assertTrue( "Then - assigned type still has element's member in where used.",
            assignedType.getWhereUsed().contains( member ) );
        assertTrue( "Then - property has name.",
            element.assignedTypeProperty().get().equals( assignedType.getName() ) );
    }
}
