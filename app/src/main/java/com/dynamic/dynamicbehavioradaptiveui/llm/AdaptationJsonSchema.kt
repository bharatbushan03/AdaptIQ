package com.dynamic.dynamicbehavioradaptiveui.llm

object AdaptationJsonSchema {
    val SCHEMA = """
        {
            "type": "object",
            "properties": {
                "action": {
                    "type": "string",
                    "enum": [
                        "SHOW_SHORTCUT",
                        "HIDE_LOW_PRIORITY_ACTION",
                        "SHOW_GUIDANCE",
                        "REORDER_SECONDARY_ACTIONS",
                        "REDUCE_INFORMATION_DENSITY",
                        "INCREASE_INFORMATION_DENSITY",
                        "HIGHLIGHT_RELEVANT_ACTION",
                        "NO_CHANGE"
                    ]
                },
                "target": {
                    "type": "string",
                    "minLength": 1
                },
                "reason": {
                    "type": "string",
                    "minLength": 1
                },
                "confidence": {
                    "type": "number",
                    "minimum": 0.0,
                    "maximum": 1.0
                },
                "expectedBenefit": {
                    "type": "string",
                    "minLength": 1
                },
                "expiration": {
                    "type": "integer",
                    "minimum": 1
                },
                "safetyLevel": {
                    "type": "string",
                    "enum": ["low", "medium", "high"]
                }
            },
            "required": [
                "action",
                "target",
                "reason",
                "confidence",
                "expectedBenefit",
                "expiration",
                "safetyLevel"
            ],
            "additionalProperties": false
        }
    """
}