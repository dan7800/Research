package org.apache.james.mime4j.message;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

public class Multipart
  implements Body
{
  private List<BodyPart> bodyParts = new LinkedList();
  private ByteSequence epilogue;
  private transient String epilogueStrCache;
  private Entity parent = null;
  private ByteSequence preamble;
  private transient String preambleStrCache;
  private String subType;

  public Multipart(String paramString)
  {
    this.preamble = ByteSequence.EMPTY;
    this.preambleStrCache = "";
    this.epilogue = ByteSequence.EMPTY;
    this.epilogueStrCache = "";
    this.subType = paramString;
  }

  public Multipart(Multipart paramMultipart)
  {
    this.preamble = paramMultipart.preamble;
    this.preambleStrCache = paramMultipart.preambleStrCache;
    this.epilogue = paramMultipart.epilogue;
    this.epilogueStrCache = paramMultipart.epilogueStrCache;
    Iterator localIterator = paramMultipart.bodyParts.iterator();
    while (localIterator.hasNext())
      addBodyPart(new BodyPart((BodyPart)localIterator.next()));
    this.subType = paramMultipart.subType;
  }

  public void addBodyPart(BodyPart paramBodyPart)
  {
    if (paramBodyPart == null)
      throw new IllegalArgumentException();
    this.bodyParts.add(paramBodyPart);
    paramBodyPart.setParent(this.parent);
  }

  public void addBodyPart(BodyPart paramBodyPart, int paramInt)
  {
    if (paramBodyPart == null)
      throw new IllegalArgumentException();
    this.bodyParts.add(paramInt, paramBodyPart);
    paramBodyPart.setParent(this.parent);
  }

  public void dispose()
  {
    Iterator localIterator = this.bodyParts.iterator();
    while (localIterator.hasNext())
      ((BodyPart)localIterator.next()).dispose();
  }

  public List<BodyPart> getBodyParts()
  {
    return Collections.unmodifiableList(this.bodyParts);
  }

  public int getCount()
  {
    return this.bodyParts.size();
  }

  public String getEpilogue()
  {
    if (this.epilogueStrCache == null)
      this.epilogueStrCache = ContentUtil.decode(this.epilogue);
    return this.epilogueStrCache;
  }

  ByteSequence getEpilogueRaw()
  {
    return this.epilogue;
  }

  public Entity getParent()
  {
    return this.parent;
  }

  public String getPreamble()
  {
    if (this.preambleStrCache == null)
      this.preambleStrCache = ContentUtil.decode(this.preamble);
    return this.preambleStrCache;
  }

  ByteSequence getPreambleRaw()
  {
    return this.preamble;
  }

  public String getSubType()
  {
    return this.subType;
  }

  public BodyPart removeBodyPart(int paramInt)
  {
    BodyPart localBodyPart = (BodyPart)this.bodyParts.remove(paramInt);
    localBodyPart.setParent(null);
    return localBodyPart;
  }

  public BodyPart replaceBodyPart(BodyPart paramBodyPart, int paramInt)
  {
    if (paramBodyPart == null)
      throw new IllegalArgumentException();
    BodyPart localBodyPart = (BodyPart)this.bodyParts.set(paramInt, paramBodyPart);
    if (paramBodyPart == localBodyPart)
      throw new IllegalArgumentException("Cannot replace body part with itself");
    paramBodyPart.setParent(this.parent);
    localBodyPart.setParent(null);
    return localBodyPart;
  }

  public void setBodyParts(List<BodyPart> paramList)
  {
    this.bodyParts = paramList;
    Iterator localIterator = paramList.iterator();
    while (localIterator.hasNext())
      ((BodyPart)localIterator.next()).setParent(this.parent);
  }

  public void setEpilogue(String paramString)
  {
    this.epilogue = ContentUtil.encode(paramString);
    this.epilogueStrCache = paramString;
  }

  void setEpilogueRaw(ByteSequence paramByteSequence)
  {
    this.epilogue = paramByteSequence;
    this.epilogueStrCache = null;
  }

  public void setParent(Entity paramEntity)
  {
    this.parent = paramEntity;
    Iterator localIterator = this.bodyParts.iterator();
    while (localIterator.hasNext())
      ((BodyPart)localIterator.next()).setParent(paramEntity);
  }

  public void setPreamble(String paramString)
  {
    this.preamble = ContentUtil.encode(paramString);
    this.preambleStrCache = paramString;
  }

  void setPreambleRaw(ByteSequence paramByteSequence)
  {
    this.preamble = paramByteSequence;
    this.preambleStrCache = null;
  }

  public void setSubType(String paramString)
  {
    this.subType = paramString;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.Multipart
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */