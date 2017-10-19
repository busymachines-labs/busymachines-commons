package busymachines.jsonbare

import busymachines.json.{DefaultTypeDiscriminatorConfig, JsonSyntax, SemiAutoDerivation}

object syntax extends JsonSyntax

object auto extends DefaultTypeDiscriminatorConfig with io.circe.generic.extras.AutoDerivation

object semiauto extends DefaultTypeDiscriminatorConfig with SemiAutoDerivation