package org.apache.maven.plugins.scripting;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Evaluates a script held in a resource. Use the engine name to override the engine if the resource name does not
 * refer/decode to a valid engine name or not define any at all.
 * 
 * @author Robert Scholte
 */
public class ResourceScriptEvaluator extends AbstractScriptEvaluator
{
    
  /**
   * Not null, existing readable file with the script
   */
  private final String resourceName;

  /**
   * Possibly null engine name
   */
  private final String engineName;

  /**
   * @param engineName optional engine name, used to override the engine selection from the file extension
   * @param resourceName not null
   */
  public ResourceScriptEvaluator( String engineName, String resourceName )
  {
    this.resourceName = resourceName;

    this.engineName = engineName;
  }

  /**
   * @param engine the script engine.
   * @param context the script context.
   * @return the result of the scriptFile.
   * @throws ScriptException if an error occurs in script.
   * @see org.apache.maven.plugins.scripting.AbstractScriptEvaluator#eval(javax.script.ScriptEngine, javax.script.ScriptContext)
   */
  protected Object eval( ScriptEngine engine, ScriptContext context ) throws ScriptException
  {
      
    try ( InputStream is = this.getClass().getClassLoader().getResourceAsStream( resourceName );
          Reader reader = new InputStreamReader( is ) )
    {
        return engine.eval( reader, context );
    }
    catch ( IOException ex )
    {
      throw new UncheckedIOException( resourceName + " caused:", ex );
    }
  }

  /**
   * Gets the script engine by engineName, otherwise by extension of the sciptFile 
   * 
   * @param manager the script engine manager
   * @throws UnsupportedScriptEngineException if specified engine is not available 
   * @see org.apache.maven.plugins.scripting.AbstractScriptEvaluator#getEngine(javax.script.ScriptEngineManager)
   */
  protected ScriptEngine getEngine( ScriptEngineManager manager ) throws UnsupportedScriptEngineException
  {
    ScriptEngine result;

    if ( engineName != null && !engineName.isEmpty() )
    {
      result = manager.getEngineByName( engineName );

      if ( result == null )
      {
        throw new UnsupportedScriptEngineException( "No engine found by name \"" + engineName + "\n" );
      }
    }
    else
    {
      String name = resourceName;
      int fileSepIndex = name.lastIndexOf( '/' ); 
      if ( fileSepIndex >= 0 )
      {
          name = name.substring( fileSepIndex + 1 );
      }
      
      String extension = name;
      int position = name.indexOf( "." );
      if ( position >= 0 )
      {
        extension = name.substring( position + 1 );
      }
      result = manager.getEngineByExtension( extension );

      if ( result == null )
      {
        throw new UnsupportedScriptEngineException( "No engine found by extension \"" + extension + "\n" );
      }
    }
    return result;
  }
}
