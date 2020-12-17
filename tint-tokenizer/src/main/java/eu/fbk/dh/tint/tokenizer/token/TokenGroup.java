package eu.fbk.dh.tint.tokenizer.token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by alessio on 14/07/16.
 */

public class TokenGroup {

    private ArrayList<Token> support = new ArrayList<>();
    private HashMap<Integer, Token> startIndexes = new HashMap<>();
    private HashMap<Integer, Token> endIndexes = new HashMap<>();
    private HashMap<Integer, Token> startOffIndexes = new HashMap<>();
    private HashMap<Integer, Token> endOffIndexes = new HashMap<>();
    private Set<Integer> newLines = new HashSet<>();

    public void addToken(Token token) {
        if (!token.hasSpaceBefore() && support.size() > 0) {
            support.get(support.size() - 1).setHasSpaceAfter(false);
        }
        support.add(token);
        startIndexes.put(token.getStart(), token);
        endIndexes.put(token.getEnd(), token);
        startOffIndexes.put(token.getStart() + token.getSpaceOffset(), token);
        endOffIndexes.put(token.getStart() + token.getSpaceOffset(), token);
    }

    public void addNewLine(int offset) {
        newLines.add(offset);
    }

    public ArrayList<Token> getSupport() {
        return support;
    }

    public HashMap<Integer, Token> getStartIndexes() {
        return startIndexes;
    }

    public HashMap<Integer, Token> getEndIndexes() {
        return endIndexes;
    }

    public HashMap<Integer, Token> getStartOffIndexes() {
        return startOffIndexes;
    }

    public HashMap<Integer, Token> getEndOffIndexes() {
        return endOffIndexes;
    }

    public Set<Integer> getNewLines() {
        return newLines;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("TokenGroup{").append("\n");
        for (Token token : support) {
            buffer.append(" ").append(token.toString()).append("\n");
        }
        buffer.append("}");
        return buffer.toString();
    }
}
