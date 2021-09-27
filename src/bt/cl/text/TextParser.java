package bt.cl.text;

import bt.log.Logger;

public class TextParser
{
    private static final String START_TAG = "<+bt";
    private static final String END_TAG = "<-bt>";

    public StyledTextNode parse(String text)
    {
        StyledTextNode parent = new StyledTextNode();
        parseNode(text, parent);

        return parent;
    }

    private int parseNode(String text, StyledTextNode parent)
    {
        Logger.global().entry(text);
        StyledTextNode node = new StyledTextNode();
        parent.addChild(node);

        int textIndex = 0;
        int startTagIndex = 0;
        int startTagEndIndex = 0;
        int endTagIndex = 0;

        startTagIndex = text.indexOf(START_TAG, textIndex);

        if (startTagIndex == -1)
        {
            endTagIndex = text.indexOf(END_TAG);

            if (endTagIndex != -1)
            {
                node.setText(text.substring(0, endTagIndex));

                if (parent.getParent() != null)
                {
                    textIndex = parseNode(text.substring(endTagIndex + END_TAG.length()), parent.getParent());
                }
                else
                {
                    throw new IllegalArgumentException("Invalid closing tag found: " + text);
                }
            }
            else
            {
                node.setText(text);
                textIndex = -1;
            }
        }
        else if (startTagIndex != 0)
        {
            node.setText(text.substring(0, startTagIndex));
            textIndex = parseNode(text.substring(startTagIndex), parent);
        }
        else
        {
            while (textIndex != -1)
            {
                startTagEndIndex = text.indexOf(">", textIndex);
                String[] tags = text.substring(startTagIndex + START_TAG.length(), startTagEndIndex).trim().split(" ");
                node.addStyles(tags);

                // check if there is more tags before the next closing tag
                startTagIndex = text.indexOf(START_TAG, startTagEndIndex);
                endTagIndex = text.indexOf(END_TAG, startTagEndIndex);

                if (startTagIndex != -1 && startTagIndex < endTagIndex)
                {
                    textIndex = parseNode(text.substring(startTagEndIndex + 1), node);
                }
                else if (endTagIndex != -1)
                {
                    textIndex = parseNode(text.substring(startTagEndIndex + 1, endTagIndex), node);
                    textIndex = parseNode(text.substring(endTagIndex + END_TAG.length()), parent);
                }
            }
        }

        return textIndex;
    }
}